package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Phone number format is invalid")
    private String phone;

    // Optional password change for customer
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
