package com.fashion.leon.fashionshopbackend.dto;

import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
}
