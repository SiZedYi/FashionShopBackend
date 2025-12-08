package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.CategoryRequest;
import com.fashion.leon.fashionshopbackend.dto.CategoryResponse;
import com.fashion.leon.fashionshopbackend.dto.CategoryUpdateRequest;
import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN', 'MANAGER')")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public PaginatedResponse<CategoryResponse> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return categoryService.listAll(pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return categoryService.get(id);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        public ResponseEntity<?> create(
                @RequestPart("data") String data,
                @RequestPart(value = "image", required = false) MultipartFile image) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                CategoryRequest request = objectMapper.readValue(data, CategoryRequest.class);
                // Validate manually
                java.util.List<String> errors = new java.util.ArrayList<>();
                if (request.getName() == null || request.getName().trim().isEmpty()) {
                    errors.add("Category name is required");
                }
                if (!errors.isEmpty()) {
                    return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("name", errors.get(0)));
                }
                CategoryResponse res = categoryService.create(request, image);
                return ResponseEntity.status(HttpStatus.CREATED).body(res);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Invalid category data"));
            }
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            CategoryUpdateRequest request = objectMapper.readValue(data, CategoryUpdateRequest.class);
            // Validate manually
            java.util.List<String> errors = new java.util.ArrayList<>();
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                errors.add("Category name is required");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("name", errors.get(0)));
            }
            categoryService.update(id, request, image);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Invalid category data"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean deleted = categoryService.deleteIfNoProducts(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Collections.singletonMap("error", "Không xóa được category vì đã có sản phẩm liên quan"));
        }
    }
}
