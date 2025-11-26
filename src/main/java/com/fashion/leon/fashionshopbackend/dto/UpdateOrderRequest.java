package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderRequest {

    @Valid
    private ShippingAddressRequest shippingAddress;

    private List<UpdateOrderItemRequest> items;
}
