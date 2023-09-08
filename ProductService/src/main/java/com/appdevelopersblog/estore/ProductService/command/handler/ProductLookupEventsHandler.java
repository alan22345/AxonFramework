package com.appdevelopersblog.estore.ProductService.command.handler;

import com.appdevelopersblog.estore.ProductService.core.data.ProductLookupEntity;
import com.appdevelopersblog.estore.ProductService.core.event.ProductCreatedEvent;
import com.appdevelopersblog.estore.ProductService.core.repo.ProductLookupRepository;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

    private final ProductLookupRepository productLookupRepository;

    public ProductLookupEventsHandler(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event){
        ProductLookupEntity productLookupEntity = new ProductLookupEntity(event.getProductId(),
            event.getTitle());
        productLookupRepository.save(productLookupEntity);
    }
}
