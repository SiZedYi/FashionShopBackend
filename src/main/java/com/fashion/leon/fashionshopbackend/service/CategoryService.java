package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.CategoryRequest;
import com.fashion.leon.fashionshopbackend.dto.CategoryResponse;
import com.fashion.leon.fashionshopbackend.dto.CategoryUpdateRequest;
import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.entity.Category;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.mapper.CategoryMapper;
import com.fashion.leon.fashionshopbackend.repository.CategoryRepository;
import com.fashion.leon.fashionshopbackend.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final com.fashion.leon.fashionshopbackend.service.FileStorageService fileStorageService;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    // Public: list only active categories
    public PaginatedResponse<CategoryResponse> listActive(Pageable pageable) {
        Page<Category> page = categoryRepository.findByIsActiveTrue(pageable);
        List<CategoryResponse> data = page.getContent().stream().map(categoryMapper::toResponse).collect(Collectors.toList());
        return new PaginatedResponse<>(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                data
        );
    }

    // Admin: list all categories
    public PaginatedResponse<CategoryResponse> listAll(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        List<CategoryResponse> data = page.getContent().stream().map(categoryMapper::toResponse).collect(Collectors.toList());
        return new PaginatedResponse<>(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                data
        );
    }

    public CategoryResponse get(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponse(c);
    }

    // Overload: create with uploaded image; save under images/category and store path only (no host)
    public CategoryResponse create(CategoryRequest req, MultipartFile image) {
        String name = req.getName().trim();
        String slug = normalizeSlug(req.getSlug() == null || req.getSlug().isBlank() ? name : req.getSlug());
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug already exists: " + slug);
        }

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            String filename = fileStorageService.save(image, "images/category");
            imagePath = "/images/category/" + filename; // store path only
        }

        Category c = Category.builder()
                .name(name)
                .slug(slug)
                .description(req.getDescription())
                .images(imagePath)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(c));
    }

    public void update(Long id, CategoryUpdateRequest req, MultipartFile image) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (req.getName() != null) c.setName(req.getName().trim());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());
        if (req.getSlug() != null) {
            String slug = normalizeSlug(req.getSlug().isBlank() ? c.getName() : req.getSlug());
            if (!slug.equalsIgnoreCase(c.getSlug()) && categoryRepository.existsBySlug(slug)) {
                throw new IllegalArgumentException("Slug already exists: " + slug);
            }
            c.setSlug(slug);
        }
        // Handle image update
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (c.getImages() != null && !c.getImages().isBlank()) {
                String filename = c.getImages().substring(c.getImages().lastIndexOf("/") + 1);
                try { fileStorageService.delete(filename, "images/category"); } catch (Exception e) { }
            }
            String filename = fileStorageService.save(image, "images/category");
            // Ensure only one /images/category/ prefix
            String imagePath = filename.startsWith("/images/category/") ? filename : "/images/category/" + filename;
            c.setImages(imagePath);
        }
        c.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(c);
    }

    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(c);
    }

    public boolean deleteIfNoProducts(Long id) {
        long count = productCategoryRepository.countByCategoryId(id);
        if (count > 0) return false;
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(c);
        return true;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\u0000-\u007F]");
    private static final Pattern NONALNUM = Pattern.compile("[^a-z0-9]+");

    private String normalizeSlug(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String latin = NONLATIN.matcher(normalized).replaceAll("");
        String slug = NONALNUM.matcher(latin.toLowerCase(Locale.ROOT)).replaceAll("-").replaceAll("-+", "-");
        slug = slug.replaceAll("(^-)|(-$)", "");
        return slug;
    }
}
