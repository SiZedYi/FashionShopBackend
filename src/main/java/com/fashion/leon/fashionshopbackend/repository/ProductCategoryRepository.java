package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.ProductCategory;
import com.fashion.leon.fashionshopbackend.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    @Query("SELECT COUNT(pc) FROM ProductCategory pc WHERE pc.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}
