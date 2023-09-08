package com.appsdeveloperblog.estore.OrdersService.saga;

import com.appsdeveloperblog.estore.OrdersService.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.estore.OrdersService.command.commands.RejectOrderCommand;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.model.OrderSummary;
import com.appsdeveloperblog.estore.OrdersService.query.FindOrderQuery;
import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
public class OrderSaga {
    @Autowired
    private transient CommandGateway commandGateway; // transient means command gateway won't be serialised
    @Autowired
    private transient QueryGateway queryGateway;
    @Autowired
    private transient DeadlineManager deadlineManager;
    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;
    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE="payment-processing-deadline";

    private String deadlineId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent){
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                                 CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    //start compensatnig transaction
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());
                    commandGateway.send(rejectOrderCommand);
                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent){
        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
        User userPaymentDetails = null;
        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception e){
            cancelProductReservation(productReservedEvent,e.getMessage());
            return;
        }
        if(userPaymentDetails == null){
            cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
            return;
        }

        deadlineId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS), PAYMENT_PROCESSING_TIMEOUT_DEADLINE
                , userPaymentDetails);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception ex){
            cancelProductReservation(productReservedEvent,ex.getMessage());
            return;
        }
        if(result == null){
            cancelProductReservation(productReservedEvent,"could not process payment");
            return;
        }
    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason){

        cancelDeadline();

        CancelProductReservationCommand publishProductReservationCommand =
                CancelProductReservationCommand.builder()
                        .orderId(productReservedEvent.getOrderId())
                        .productId(productReservedEvent.getProductId())
                        .quantity(productReservedEvent.getQuantity())
                        .userId(productReservedEvent.getUserId())
                        .reason(reason)
                        .build();
        commandGateway.send(publishProductReservationCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent){

        cancelDeadline();
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand((paymentProcessedEvent.getOrderId()));
        commandGateway.send(approveOrderCommand);
    }

    private void cancelDeadline(){
        if(deadlineId != null){
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, deadlineId);
            deadlineId =null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent){
        //SagaLifecycle.end();
        queryUpdateEmitter.emit(FindOrderQuery.class, query-> true, new OrderSummary(orderApprovedEvent.getOrderId(),
                orderApprovedEvent.getOrderStatus(),
                ""));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent){
        //Create and send a RejectOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent){
        //successfully finished rollback
        queryUpdateEmitter.emit(FindOrderQuery.class, query-> true, new OrderSummary(orderRejectedEvent.getOrderId(),
                orderRejectedEvent.getOrderStatus(),
                orderRejectedEvent.getReason()));
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        // payment processing deadline took place
        // send compensating command to cancel payment
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
