package com.fashion.leon.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryId implements Serializable {
    private Long productId;
    private Integer categoryId;
}

