package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Status cannot be blank")
    private String status;
}
