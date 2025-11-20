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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// import removed: we now store relative image paths and no longer build absolute URLs

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable,
                                 String category,
                                 String color,
                                 java.math.BigDecimal minPrice,
                                 java.math.BigDecimal maxPrice) {
    String catFilter = (category != null && !category.isBlank()) ? category : null;
    String colorFilter = (color != null && !color.isBlank()) ? color : null;
    Page<Product> productPage = productRepository.searchProducts(catFilter, colorFilter, minPrice, maxPrice, pageable);
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
            ProductCategory savedPc = productCategoryRepository.save(productCategory);
            log.info("[PRODUCT CREATE] Linked productId={} to categoryId={} (pcId=({},{}))",
                    savedProduct.getId(), category.getId(), savedPc.getId().getProductId(), savedPc.getId().getCategoryId());
        }

        // Handle images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile imageFile : images) {
                String filename = fileStorageService.save(imageFile, "images/products");
                // Ensure we only keep the base file name in case storage returns a path
                String baseName = filename;
                int slash = Math.max(baseName.lastIndexOf('/'), baseName.lastIndexOf('\\'));
                if (slash >= 0) baseName = baseName.substring(slash + 1);
                String imagePath = "/images/products/" + baseName;

                ProductImage productImage = new ProductImage();
                productImage.setProduct(savedProduct);
                productImage.setUrl(imagePath);
                productImageRepository.save(productImage);
            }
        }

        return productMapper.toProductResponse(productRepository.findById(savedProduct.getId()).get());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, List<MultipartFile> images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        log.info("Product before update: id={}, name={}", product.getId(), product.getName());
        if (productRequest.getName() != null) product.setName(productRequest.getName());
        if (productRequest.getDescription() != null) product.setDescription(productRequest.getDescription());
        if (productRequest.getPrice() != null) product.setPrice(productRequest.getPrice());
        if (productRequest.getSalePrice() != null) product.setSalePrice(productRequest.getSalePrice());
        if (productRequest.getSku() != null) product.setSku(productRequest.getSku());
        if (productRequest.getStockQuantity() != null) product.setStockQuantity(productRequest.getStockQuantity());
        if (productRequest.getIsActive() != null) product.setIsActive(productRequest.getIsActive());
        if (productRequest.getAboutItem() != null) product.setAboutItem(productRequest.getAboutItem());
        if (productRequest.getDiscount() != null) product.setDiscount(productRequest.getDiscount());
        if (productRequest.getRating() != null) product.setRating(productRequest.getRating());
        if (productRequest.getBrand() != null) product.setBrand(productRequest.getBrand());
        if (productRequest.getColor() != null) product.setColor(productRequest.getColor());
    product.setUpdatedAt(LocalDateTime.now());
        // Update categories only if provided (null means no change; empty list means remove all)
        if (productRequest.getCategoryIds() != null) {
            // Desired category IDs (may be empty list to remove all)
            List<Long> categoryIds = productRequest.getCategoryIds();
            // Build a set for quick lookup and to avoid duplicates
            java.util.LinkedHashSet<Long> desiredIds = new java.util.LinkedHashSet<>();
            if (categoryIds != null) desiredIds.addAll(categoryIds);

            // Index existing links by categoryId
            java.util.Map<Long, ProductCategory> existingByCat = new java.util.HashMap<>();
            for (ProductCategory pc : new ArrayList<>(product.getProductCategories())) {
                Long catId = pc.getId() != null ? pc.getId().getCategoryId() : (pc.getCategory() != null ? pc.getCategory().getId() : null);
                if (catId != null) existingByCat.put(catId, pc);
            }

            // Remove links that are no longer desired
            for (ProductCategory pc : new ArrayList<>(product.getProductCategories())) {
                Long catId = pc.getId() != null ? pc.getId().getCategoryId() : (pc.getCategory() != null ? pc.getCategory().getId() : null);
                if (catId == null || !desiredIds.contains(catId)) {
                    product.getProductCategories().remove(pc);
                    productCategoryRepository.delete(pc);
                    log.info("[PRODUCT UPDATE] Unlinked productId={} from categoryId={}", product.getId(), catId);
                }
            }

            // Add missing links
            java.util.List<Long> toAdd = new java.util.ArrayList<>();
            for (Long desiredId : desiredIds) {
                if (!existingByCat.containsKey(desiredId)) toAdd.add(desiredId);
            }

            if (!toAdd.isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(toAdd);
                if (categories.size() != toAdd.size()) {
                    throw new ResourceNotFoundException("One or more categories not found");
                }
                for (Category category : categories) {
                    ProductCategory productCategory = new ProductCategory();
                    productCategory.setProduct(product);
                    productCategory.setCategory(category);
                    productCategory.setId(new ProductCategoryId(product.getId(), category.getId()));
                    product.getProductCategories().add(productCategory);
                    ProductCategory savedPc = productCategoryRepository.save(productCategory);
                    log.info("[PRODUCT UPDATE] Linked productId={} to categoryId={} (pcId=({},{}))",
                            product.getId(), category.getId(), savedPc.getId().getProductId(), savedPc.getId().getCategoryId());
                }
            }
        }

        // Update images
        if (images != null && !images.isEmpty()) {
            // Delete old images from storage and DB safely
            List<ProductImage> oldImages = new ArrayList<>(product.getProductImages());
            for (ProductImage oldImage : oldImages) {
                try {
                    String filename = oldImage.getUrl().substring(oldImage.getUrl().lastIndexOf("/") + 1);
                    fileStorageService.delete(filename, "images/products");
                } catch (Exception e) {
                    // ignore file delete errors
                }
            }
            // Clear in-memory collection first to avoid merging deleted instances
            product.getProductImages().clear();
            if (!oldImages.isEmpty()) {
                productImageRepository.deleteAll(oldImages);
            }

            // Add new images
            for (MultipartFile imageFile : images) {
                String filename = fileStorageService.save(imageFile, "images/products");
                // Ensure we only keep the base file name in case storage returns a path
                String baseName = filename;
                int slash = Math.max(baseName.lastIndexOf('/'), baseName.lastIndexOf('\\'));
                if (slash >= 0) baseName = baseName.substring(slash + 1);
                String imagePath = "/images/products/" + baseName;

                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setUrl(imagePath);
                // maintain both sides
                product.getProductImages().add(productImage);
                productImageRepository.save(productImage);
            }
        }

        // Always bump updatedAt at the end to ensure dirty state
        product.setUpdatedAt(LocalDateTime.now());
        // Save and flush to force DB update of the product row when fields changed
        Product updatedProduct = productRepository.saveAndFlush(product);
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
