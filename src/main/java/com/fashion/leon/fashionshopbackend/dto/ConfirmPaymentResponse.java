package com.fashion.leon.fashionshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentResponse {
    private Boolean success;
    private Long orderId;
    private String orderNumber;
    private String message;
}
