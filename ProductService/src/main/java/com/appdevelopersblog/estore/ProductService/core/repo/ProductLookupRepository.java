package com.appdevelopersblog.estore.ProductService.core.repo;

import com.appdevelopersblog.estore.ProductService.core.data.ProductLookupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLookupRepository extends JpaRepository<ProductLookupEntity, String> {
    ProductLookupEntity findByProductIdOrTitle(String productId, String title);
}
