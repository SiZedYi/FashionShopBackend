package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String slug; // optional; will be auto-generated from name if blank
    private String description;
    private String images;
    private Boolean isActive;
}
