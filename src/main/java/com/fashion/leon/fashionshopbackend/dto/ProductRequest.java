package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description can be up to 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
    private BigDecimal salePrice;

    @NotBlank(message = "SKU is required")
    private String sku;

    private Boolean isActive;

    private String aboutItem;

    private Integer discount;

    private Double rating;

    private String brand;

    /**
     * Comma-separated color names, e.g. "white,gray,blue,silver"
     */
    private String color;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "At least one category ID is required")
    private List<Long> categoryIds;
}
