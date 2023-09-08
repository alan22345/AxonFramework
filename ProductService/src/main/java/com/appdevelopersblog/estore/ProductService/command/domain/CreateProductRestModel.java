package com.appdevelopersblog.estore.ProductService.command.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRestModel {

    @NotBlank(message = "Product title is a required field")
    private String title;
    @Min(value=1, message = "Price cannot be less than one")
    private BigDecimal price;
    @Max(value=5, message = "Quantity cannot be more than 5")
    private Integer quantity;
}
