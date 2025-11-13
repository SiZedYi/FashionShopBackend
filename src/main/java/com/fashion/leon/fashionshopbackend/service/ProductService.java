package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.ProductRequest;
import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.entity.*;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.mapper.ProductMapper;
import com.fashion.leon.fashionshopbackend.repository.CategoryRepository;
import com.fashion.leon.fashionshopbackend.repository.ProductCategoryRepository;
import com.fashion.leon.fashionshopbackend.repository.ProductImageRepository;
import com.fashion.leon.fashionshopbackend.repository.ProductRepository;
import com.fashion.leon.fashionshopbackend.service.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponse> data = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    return new PaginatedResponse<>(
        productPage.getNumber() + 1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                data
        );
    }

    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> images) {
    Product product = new Product();
    product.setName(productRequest.getName());
    product.setDescription(productRequest.getDescription());
    product.setPrice(productRequest.getPrice());
    product.setSalePrice(productRequest.getSalePrice());
    product.setSku(productRequest.getSku());
    product.setStockQuantity(productRequest.getStockQuantity());
    product.setIsActive(false);
    product.setAboutItem(productRequest.getAboutItem());
    product.setDiscount(productRequest.getDiscount());
    product.setRating(0.0);
    product.setBrand(productRequest.getBrand());
    product.setColor(productRequest.getColor());
    product.setCreatedAt(LocalDateTime.now());
    product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Handle categories
        List<Long> categoryIds = productRequest.getCategoryIds();
        List<Category> categories = new ArrayList<>();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
        }

        for (Category category : categories) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProduct(savedProduct);
            productCategory.setCategory(category);
            productCategory.setId(new ProductCategoryId(savedProduct.getId(), category.getId()));
            productCategoryRepository.save(productCategory);
        }

        // Handle images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile imageFile : images) {
                String filename = fileStorageService.save(imageFile, "images/products");
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/products/")
                        .path(filename)
                        .toUriString();

                ProductImage productImage = new ProductImage();
                productImage.setProduct(savedProduct);
                productImage.setUrl(imageUrl);
                productImageRepository.save(productImage);
            }
        }

        return productMapper.toProductResponse(productRepository.findById(savedProduct.getId()).get());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, List<MultipartFile> images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

    product.setName(productRequest.getName());
    product.setDescription(productRequest.getDescription());
    product.setPrice(productRequest.getPrice());
    product.setSalePrice(productRequest.getSalePrice());
    product.setSku(productRequest.getSku());
    product.setStockQuantity(productRequest.getStockQuantity());
    product.setIsActive(false);
    product.setAboutItem(productRequest.getAboutItem());
    product.setDiscount(productRequest.getDiscount());
    product.setRating(0.0);
    product.setBrand(productRequest.getBrand());
    product.setColor(productRequest.getColor());
    product.setUpdatedAt(LocalDateTime.now());

        // Update categories
        productCategoryRepository.deleteAll(product.getProductCategories());
        List<Long> categoryIds = productRequest.getCategoryIds();
        List<Category> categories = new ArrayList<>();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
        }
        for (Category category : categories) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProduct(product);
            productCategory.setCategory(category);
            productCategory.setId(new ProductCategoryId(product.getId(), category.getId()));
            productCategoryRepository.save(productCategory);
        }

        // Update images
        if (images != null && !images.isEmpty()) {
            // Delete old images
            for (ProductImage oldImage : product.getProductImages()) {
                try {
                    String filename = oldImage.getUrl().substring(oldImage.getUrl().lastIndexOf("/") + 1);
                    fileStorageService.delete(filename, "images/products");
                } catch (Exception e) {
                    // Log error, but don't break the process
                }
            }
            productImageRepository.deleteAll(product.getProductImages());

            // Add new images
            for (MultipartFile imageFile : images) {
                String filename = fileStorageService.save(imageFile, "images/products");
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/products/")
                        .path(filename)
                        .toUriString();

                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setUrl(imageUrl);
                productImageRepository.save(productImage);
            }
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Delete images from storage
        for (ProductImage image : product.getProductImages()) {
            try {
                String filename = image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1);
                fileStorageService.delete(filename, "images/products");
            } catch (Exception e) {
                // Log error
            }
        }
        productRepository.deleteById(id);
    }
}
