package com.fashion.leon.fashionshopbackend.controller;


import com.fashion.leon.fashionshopbackend.dto.AuthResponse;
import com.fashion.leon.fashionshopbackend.dto.LoginRequest;
import com.fashion.leon.fashionshopbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminAuthController {
    private final UserService userService;
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

}
