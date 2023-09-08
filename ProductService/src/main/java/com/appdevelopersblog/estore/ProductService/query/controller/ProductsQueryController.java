package com.appdevelopersblog.estore.ProductService.query.controller;

import com.appdevelopersblog.estore.ProductService.command.domain.CreateProductRestModel;
import com.appdevelopersblog.estore.ProductService.query.FindProductsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsQueryController {

    @Autowired
    QueryGateway queryGateway;

    @GetMapping
    public List<CreateProductRestModel> getProducts(){
        FindProductsQuery findProductsQuery = new FindProductsQuery();
        List<CreateProductRestModel> product = queryGateway.query(findProductsQuery, ResponseTypes
                .multipleInstancesOf(CreateProductRestModel.class)).join();

        return product;
    }
}
