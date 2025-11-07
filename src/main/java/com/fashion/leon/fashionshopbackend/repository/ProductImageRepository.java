package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
