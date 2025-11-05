package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.AuthResponse;
import com.fashion.leon.fashionshopbackend.dto.LoginRequest;
import com.fashion.leon.fashionshopbackend.dto.RegisterRequest;
import com.fashion.leon.fashionshopbackend.dto.UserResponse;
import com.fashion.leon.fashionshopbackend.service.CustomerService;
import com.fashion.leon.fashionshopbackend.service.EmailService;
import com.fashion.leon.fashionshopbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final CustomerService customerService;
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        log.info("Customer registration attempt for email: {}", request.getEmail());

        try {
            AuthResponse response = customerService.registerCustomer(request);
            log.info("Customer registration successful for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Customer registration failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginCustomer(@Valid @RequestBody LoginRequest request) {
        log.info("Customer login attempt for email: {}", request.getEmail());

        try {
            AuthResponse response = customerService.loginCustomer(request);
            log.info("Customer login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Customer login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        UserResponse userResponse = customerService.getCustomerProfile(email);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint is working!");
    }

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            emailService.sendWelcomeEmail(email, "Test User");
            return ResponseEntity.ok("Test email sent successfully to: " + email);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send test email: " + e.getMessage());
        }
    }
}