package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
