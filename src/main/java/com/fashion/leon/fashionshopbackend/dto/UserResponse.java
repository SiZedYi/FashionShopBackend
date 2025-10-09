package com.fashion.leon.fashionshopbackend.dto;

import com.fashion.leon.fashionshopbackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private User.Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}