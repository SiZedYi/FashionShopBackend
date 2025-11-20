package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT DISTINCT p FROM Product p " +
	    "LEFT JOIN p.productCategories pc " +
	    "LEFT JOIN pc.category c " +
	    "WHERE (:category IS NULL OR LOWER(c.name) = LOWER(:category)) " +
	    "AND (:color IS NULL OR (:color IS NOT NULL AND LOWER(p.color) LIKE CONCAT('%', LOWER(:color), '%'))) " +
	    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
	    "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
	    countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
		    "LEFT JOIN p.productCategories pc " +
		    "LEFT JOIN pc.category c " +
		    "WHERE (:category IS NULL OR LOWER(c.name) = LOWER(:category)) " +
		    "AND (:color IS NULL OR (:color IS NOT NULL AND LOWER(p.color) LIKE CONCAT('%', LOWER(:color), '%'))) " +
		    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
		    "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(@Param("category") String category,
				 @Param("color") String color,
				 @Param("minPrice") java.math.BigDecimal minPrice,
				 @Param("maxPrice") java.math.BigDecimal maxPrice,
				 Pageable pageable);
}

