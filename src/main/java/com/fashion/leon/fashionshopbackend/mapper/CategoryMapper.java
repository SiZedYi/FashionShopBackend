package com.fashion.leon.fashionshopbackend.mapper;

import com.fashion.leon.fashionshopbackend.dto.CategoryResponse;
import com.fashion.leon.fashionshopbackend.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .images(c.getImages())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
