package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.ProductCategory;
import com.fashion.leon.fashionshopbackend.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
}
