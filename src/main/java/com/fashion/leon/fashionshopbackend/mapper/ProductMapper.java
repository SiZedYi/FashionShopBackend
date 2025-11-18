package com.fashion.leon.fashionshopbackend.mapper;

import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.entity.Product;
import com.fashion.leon.fashionshopbackend.entity.Category;
import com.fashion.leon.fashionshopbackend.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public ProductResponse toProductResponse(Product product) {
        // First category name (backward-compat)
        String category = product.getProductCategories() != null && !product.getProductCategories().isEmpty()
                ? Optional.ofNullable(product.getProductCategories().getFirst().getCategory()).map(Category::getName).orElse(null)
                : null;
        // All category names
        List<String> categories = product.getProductCategories() != null
                ? product.getProductCategories().stream()
                    .map(pc -> pc.getCategory())
                    .filter(java.util.Objects::nonNull)
                    .map(Category::getName)
                    .collect(Collectors.toList())
                : Collections.emptyList();
        // All image URLs
        List<String> images = product.getProductImages() != null
                ? product.getProductImages().stream().map(ProductImage::getUrl).collect(Collectors.toList())
                : Collections.emptyList();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(category)
                .categories(categories)
                .description(product.getDescription())
                .aboutItem(product.getAboutItem())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .rating(product.getRating())
                .stockItems(product.getStockQuantity())
                .brand(product.getBrand())
                .color(product.getColor() != null && !product.getColor().isEmpty() ?
                    java.util.Arrays.stream(product.getColor().split(",")).map(String::trim).toList() : java.util.Collections.emptyList())
                .images(images)
                .build();
    }
}
