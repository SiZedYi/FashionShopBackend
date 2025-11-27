package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank
    private String fullName;
    @NotBlank
    private String phone;
    @NotBlank
    private String line1;
    private String line2;
    @NotBlank
    private String city;
    private String state;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String country;
    private Boolean isDefault;
}
