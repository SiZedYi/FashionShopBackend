package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SliderRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Subtitle is required")
    private String subtitle;

    @NotBlank(message = "Title is required")
    private String title;

    private String buttonText;

    private String buttonLink;

    private String textAlign;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private Integer displayOrder;
}

