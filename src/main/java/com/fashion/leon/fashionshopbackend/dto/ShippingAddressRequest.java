package com.fashion.leon.fashionshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressRequest {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String city;
    private String zip;
    private String country;
}
