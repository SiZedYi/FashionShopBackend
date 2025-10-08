package com.fashion.leon.fashionshopbackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String aboutItem;
    private BigDecimal price;
    private Integer discount;
    private Double rating;
    private Integer stockItems;
    private String brand;
    private List<String> color;
    private List<String> images;
}
