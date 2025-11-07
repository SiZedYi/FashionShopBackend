package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.ProductRequest;
import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public PaginatedResponse<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @ModelAttribute ProductRequest productRequest,
            @RequestPart("images") List<MultipartFile> images) {
        ProductResponse createdProduct = productService.createProduct(productRequest, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest productRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponse updatedProduct = productService.updateProduct(id, productRequest, images);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
