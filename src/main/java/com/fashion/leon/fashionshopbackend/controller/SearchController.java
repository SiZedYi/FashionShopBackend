package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.ProductResponse;
import com.fashion.leon.fashionshopbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {
    
    private final ProductService productService;

    /**
     * Search products by keyword (không cần login)
     * @param keyword - từ khóa tìm kiếm
     * @param page - số trang (mặc định 1)
     * @param size - số lượng sản phẩm mỗi trang (mặc định 10)
     * @param category - lọc theo danh mục (tùy chọn)
     * @param color - lọc theo màu sắc (tùy chọn)
     * @param minPrice - giá tối thiểu (tùy chọn)
     * @param maxPrice - giá tối đa (tùy chọn)
     * @return PaginatedResponse chứa danh sách sản phẩm
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> searchProducts(
            @RequestParam(required = true) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice) {
        
        log.info("GET /api/search - keyword: {}, page: {}, size: {}", keyword, page, size);
        
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        PaginatedResponse<ProductResponse> response = productService.getAllProducts(
            pageable, 
            category, 
            color, 
            minPrice, 
            maxPrice, 
            keyword
        );
        
        log.info("Search results: {} products found", response.getTotalElements());
        return ResponseEntity.ok(response);
    }
}
