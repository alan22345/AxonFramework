package com.appdevelopersblog.estore.ProductService.query;

import com.appdevelopersblog.estore.ProductService.command.domain.CreateProductRestModel;
import com.appdevelopersblog.estore.ProductService.core.data.ProductEntity;
import com.appdevelopersblog.estore.ProductService.core.data.ProductsRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductsQueryHandler {
    private final ProductsRepository productsRepository;

    public ProductsQueryHandler(ProductsRepository productsRepository){
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<CreateProductRestModel> findProducts(FindProductsQuery query){
        List<CreateProductRestModel> productsRest = new ArrayList<>();

        List<ProductEntity> storedProducts = productsRepository.findAll();

        for(ProductEntity productEntity: storedProducts){
            CreateProductRestModel createProductRestModel = new CreateProductRestModel();
            BeanUtils.copyProperties(productEntity, createProductRestModel);
            productsRest.add(createProductRestModel);
        }

        return productsRest;
    }
}
