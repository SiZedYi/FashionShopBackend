package com.fashion.leon.fashionshopbackend.dto;

import com.fashion.leon.fashionshopbackend.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private Order.Status status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingFee;
    private String paymentMethod;
    private String couponCode;
    private ShippingAddressResponse shippingAddress;
    private LocalDateTime placedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}
