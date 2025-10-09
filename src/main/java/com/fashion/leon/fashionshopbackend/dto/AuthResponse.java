package com.fashion.leon.fashionshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UserResponse user;
    private String message;
    
    public AuthResponse(String token, UserResponse user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }
}