package com.appdevelopersblog.estore.ProductService.core.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class ProductEntity implements Serializable{

    @Id
    @Column(unique = true)
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
