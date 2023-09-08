package com.appdevelopersblog.estore.ProductService.command.controller;

import com.appdevelopersblog.estore.ProductService.command.CreateProductCommand;
import com.appdevelopersblog.estore.ProductService.command.domain.CreateProductRestModel;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductsCommandController {

    private final Environment env;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductsCommandController(Environment env, CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@RequestBody CreateProductRestModel createProductRestModel){
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .price(createProductRestModel.getPrice())
                .title(createProductRestModel.getTitle())
                .quantity(createProductRestModel.getQuantity())
                .productId(UUID.randomUUID().toString())
                .build();

        String returnValue;

        returnValue = commandGateway.sendAndWait(createProductCommand);

//        try {
//            returnValue = commandGateway.sendAndWait(createProductCommand);
//        } catch (Exception ex){
//            returnValue = ex.getLocalizedMessage();
//        }

        return returnValue;
    }
}
