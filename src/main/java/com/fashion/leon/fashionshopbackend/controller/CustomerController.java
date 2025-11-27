package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.AddressRequest;
import com.fashion.leon.fashionshopbackend.dto.AddressResponse;
import com.fashion.leon.fashionshopbackend.dto.CustomerUpdateRequest;
import com.fashion.leon.fashionshopbackend.dto.UserResponse;
import com.fashion.leon.fashionshopbackend.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        return ResponseEntity.ok(customerService.getCustomerProfile(email));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateOwnProfile(Authentication authentication,
                                                         @Valid @RequestBody CustomerUpdateRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        UserResponse res = customerService.updateOwnProfile(email, request);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPERADMIN')")
    public ResponseEntity<UserResponse> adminUpdateCustomer(@PathVariable Long customerId,
                                                            @Valid @RequestBody CustomerUpdateRequest request) {
        UserResponse res = customerService.adminUpdateCustomer(customerId, request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPERADMIN')")
    public ResponseEntity<List<UserResponse>> getAllCustomers() {
        List<UserResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPERADMIN')")
    public ResponseEntity<UserResponse> getCustomerDetail(@PathVariable Long customerId) {
        UserResponse customer = customerService.getCustomerDetail(customerId);
        return ResponseEntity.ok(customer);
    }

    @PostMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> addAddress(Authentication authentication,
                                                   @Valid @RequestBody AddressRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        UserResponse res = customerService.addAddress(email, request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        List<AddressResponse> addresses = customerService.getAddresses(email);
        return ResponseEntity.ok(addresses);
    }
}
