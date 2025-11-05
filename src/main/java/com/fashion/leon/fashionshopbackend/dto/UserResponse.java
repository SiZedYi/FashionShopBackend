package com.fashion.leon.fashionshopbackend.dto;

import com.fashion.leon.fashionshopbackend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Set<String> roles;
    private Boolean isActive;
    private LocalDateTime createdAt;
}