package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.entity.Product;
import com.fashion.leon.fashionshopbackend.mapper.ProductMapper;
import com.fashion.leon.fashionshopbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponse> data = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                data
        );
    }

    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toProductResponse(product);
    }
}
