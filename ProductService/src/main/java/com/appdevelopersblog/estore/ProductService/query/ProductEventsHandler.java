package com.appdevelopersblog.estore.ProductService.query;

import com.appdevelopersblog.estore.ProductService.core.data.ProductEntity;
import com.appdevelopersblog.estore.ProductService.core.data.ProductsRepository;
import com.appdevelopersblog.estore.ProductService.core.event.ProductCreatedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductEventsHandler {

    private final ProductsRepository productsRepository;

    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception){
        throw exception;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception){
        // log error message
    }

    @EventHandler
    public void on(ProductCreatedEvent event){
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event,productEntity);
        try{
            productsRepository.save(productEntity);
        } catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void on(ProductReservedEvent event){
        ProductEntity productEntity = productsRepository.findByProductId(event.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() - event.getQuantity());
    }

    @EventHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent){
        ProductEntity currentlyStoredProduct = productsRepository.findByProductId(productReservationCancelledEvent.getProductId());
        int newQuantity = currentlyStoredProduct.getQuantity() + productReservationCancelledEvent.getQuantity();
        currentlyStoredProduct.setQuantity(newQuantity);
        productsRepository.save(currentlyStoredProduct);
    }
}
