package com.fashion.leon.fashionshopbackend.controller;


import com.fashion.leon.fashionshopbackend.dto.*;
import com.fashion.leon.fashionshopbackend.service.CustomerService;
import com.fashion.leon.fashionshopbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminAuthController {
    private final UserService userService;
    private final CustomerService customerService;
    // Admin login endpoint
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        log.info("Admin login attempt for email: {}", request.getEmail());

        try {
            AuthResponse response = userService.loginUser(request);
            log.info("Admin login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Admin login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    // ========== ADMIN USER MANAGEMENT ==========
    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        log.info("[ADMIN] Create user: {}", request.getEmail());
        UserResponse res = userService.createUserByAdmin(request);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody AdminUpdateUserRequest request) {
        log.info("[ADMIN] Update user id: {}", id);
        UserResponse res = userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> assignRoles(@PathVariable Long id,
                                                    @Valid @RequestBody AssignRolesRequest request) {
        log.info("[ADMIN] Assign roles to user id: {}", id);
        UserResponse res = userService.assignRolesToUser(id, request);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.info("[ADMIN] Delete user id: {}", id);
        userService.deleteUserByAdmin(id);
        return ResponseEntity.ok(Map.of(
                "message", "Xóa user thành công",
                "id", String.valueOf(id)
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> listUsers(@RequestParam(required = false) String q,
                                                                     @RequestParam(required = false) Boolean isActive,
                                                                     @RequestParam(required = false) String role,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        log.info("[ADMIN] List users q={} isActive={} role={} page={} size={}", q, isActive, role, page, size);
        PaginatedResponse<UserResponse> result = userService.listUsers(q, isActive, role, page, size);
        return ResponseEntity.ok(result);
    }

    // Delete a specific customer by id if they have never placed an order
    @DeleteMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<?> deleteCustomerIfNoOrders(@PathVariable Long customerId) {
        try {
            customerService.deleteCustomerIfNeverOrderedById(customerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Xóa customer thành công",
                    "customerId", String.valueOf(customerId)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
