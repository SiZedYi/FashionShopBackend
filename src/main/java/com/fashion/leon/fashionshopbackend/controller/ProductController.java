package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.ProductRequest;
import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public PaginatedResponse<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createProduct(
        @Valid @RequestPart("product") ProductRequest productRequest,
        @RequestPart("images") List<MultipartFile> images) {
        try {

            ProductResponse createdProduct = productService.createProduct(productRequest, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Product created successfully", createdProduct));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Product creation failed: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            log.info("Updating product with id:"  + id);
            ProductResponse updatedProduct = productService.updateProduct(id, productRequest, images);
            return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Product update failed: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Product deletion failed: " + e.getMessage(), null));
        }
    }
// Thêm class ApiResponse để chuẩn hóa phản hồi
    @lombok.Data
    @lombok.AllArgsConstructor
    static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
    }
}
