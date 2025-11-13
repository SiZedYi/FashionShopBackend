package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.CategoryResponse;
import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {
    private final CategoryService categoryService;

    // Public list: only active categories
    @GetMapping
    public PaginatedResponse<CategoryResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return categoryService.listActive(pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return categoryService.get(id);
    }
}
