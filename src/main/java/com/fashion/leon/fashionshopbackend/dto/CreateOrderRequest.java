package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDto> items;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal shippingFee;

    private String couponCode;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // "STRIPE" or "COD"
}
