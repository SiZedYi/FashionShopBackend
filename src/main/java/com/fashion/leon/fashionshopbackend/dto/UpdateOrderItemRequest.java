package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderItemRequest {

    @NotNull
    private Long itemId;

    @NotNull
    private Integer quantity;
}
