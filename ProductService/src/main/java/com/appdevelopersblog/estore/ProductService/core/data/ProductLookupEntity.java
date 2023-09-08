package com.appdevelopersblog.estore.ProductService.core.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productLookup")
public class ProductLookupEntity implements Serializable {

    @Id
    private String productId;
    @Column(unique = true)
    private String title;
}
