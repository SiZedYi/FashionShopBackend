package com.fashion.leon.fashionshopbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRolesRequest {
    @NotEmpty(message = "roles must not be empty")
    private Set<String> roles;
}
