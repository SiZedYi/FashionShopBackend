package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
