package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByCustomerIdAndPlacedAtIsNotNull(Long customerId);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Optional<Order> findByStripeSessionId(String stripeSessionId);
    
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o " +
        "WHERE (:id IS NOT NULL AND o.id = :id) " +
        "   OR (LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :q, '%'))) " +
        "   OR (:status IS NOT NULL AND o.status = :status)")
    Page<Order> searchOrders(@Param("id") Long id,
                 @Param("q") String q,
                 @Param("status") Order.Status status,
                 Pageable pageable);
}
