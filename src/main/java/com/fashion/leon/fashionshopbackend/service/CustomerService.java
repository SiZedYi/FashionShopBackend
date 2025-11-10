package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.*;
import com.fashion.leon.fashionshopbackend.entity.Customer;
import com.fashion.leon.fashionshopbackend.exception.EmailAlreadyExistsException;
import com.fashion.leon.fashionshopbackend.exception.InvalidCredentialsException;
import com.fashion.leon.fashionshopbackend.repository.CustomerRepository;
import com.fashion.leon.fashionshopbackend.repository.OrderRepository;
import com.fashion.leon.fashionshopbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
        private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
        private final NotifyService notifyService;

    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        log.info("Attempting to register customer with email: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + request.getEmail());
        }

        // Create new customer
        Customer customer = Customer.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save customer to database
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedCustomer.getEmail());

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(savedCustomer.getId())
                .email(savedCustomer.getEmail())
                .fullName(savedCustomer.getFullName())
                .phone(savedCustomer.getPhone())
                .roles(Collections.emptySet()) // Customers don't have roles
                .isActive(savedCustomer.getIsActive())
                .createdAt(savedCustomer.getCreatedAt())
                .build();

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(savedCustomer.getEmail(), savedCustomer.getFullName());

        // Notify management about new customer registration (optional)
        try {
            notifyService.notify(
                    "customer_registered",
                    "Khách hàng mới đăng ký",
                    "Customer '" + savedCustomer.getFullName() + "' (" + savedCustomer.getEmail() + ") đã đăng ký.",
                    java.util.Map.of(
                            "id", savedCustomer.getId(),
                            "email", savedCustomer.getEmail(),
                            "fullName", savedCustomer.getFullName()
                    )
            );
        } catch (Exception ex) {
            log.warn("NotifyService failed for customer_registered: {}", ex.getMessage());
        }

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng ký tài khoản thành công! Email chào mừng đang được gửi.")
                .build();
    }

    public AuthResponse loginCustomer(LoginRequest request) {
        log.info("Attempting to login customer with email: {}", request.getEmail());

        // Find customer by email
        Customer customer = customerRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

        // Check for null password hash
        if (customer.getPasswordHash() == null) {
            log.error("Customer with email {} has null password hash", request.getEmail());
            throw new InvalidCredentialsException("Tài khoản chưa thiết lập mật khẩu hoặc dữ liệu không hợp lệ");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }

        log.info("Customer login successful for email: {}", request.getEmail());

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .build();

        // Generate JWT token
        String token = jwtUtil.generateToken(customer.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng nhập thành công!")
                .build();
    }

    public UserResponse getCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        log.info("Fetching profile for customer with email: {}", customer.getEmail());

        return UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }

        @Transactional
        public void deleteCustomerIfNeverOrdered(String email) {
                Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                long orderCount = orderRepository.countByCustomerIdAndPlacedAtIsNotNull(customer.getId());
                if (orderCount > 0) {
                        throw new IllegalStateException("Không thể xóa tài khoản vì đã từng đặt hàng online.");
                }

                customer.setIsActive(false);
                customer.setDeletedAt(java.time.LocalDateTime.now());
                customer.setUpdatedAt(java.time.LocalDateTime.now());
                customerRepository.save(customer);
                log.info("Customer {} deleted (soft) due to no order history", email);
        }

                @Transactional
                public void deleteCustomerIfNeverOrderedById(Long customerId) {
                        Customer customer = customerRepository.findById(customerId)
                                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                        if (Boolean.FALSE.equals(customer.getIsActive())) {
                                throw new IllegalStateException("Customer đã bị vô hiệu hóa trước đó");
                        }
                        long orderCount = orderRepository.countByCustomerIdAndPlacedAtIsNotNull(customer.getId());
                        if (orderCount > 0) {
                                throw new IllegalStateException("Không thể xóa customer vì đã từng đặt hàng online.");
                        }
                        customer.setIsActive(false);
                        customer.setDeletedAt(java.time.LocalDateTime.now());
                        customer.setUpdatedAt(java.time.LocalDateTime.now());
                        customerRepository.save(customer);
                        log.info("Customer id={} deleted (soft) by admin due to no order history", customerId);
                }

                        @Transactional
                        public UserResponse updateOwnProfile(String email, CustomerUpdateRequest request) {
                                Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                                boolean changed = false;
                                if (request.getFullName() != null) { customer.setFullName(request.getFullName()); changed = true; }
                                if (request.getPhone() != null) { customer.setPhone(request.getPhone()); changed = true; }
                                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                                        customer.setPasswordHash(passwordEncoder.encode(request.getPassword())); changed = true; }
                                if (changed) {
                                        customer.setUpdatedAt(LocalDateTime.now());
                                        customerRepository.save(customer);
                                }
                                return UserResponse.builder()
                                                .id(customer.getId())
                                                .email(customer.getEmail())
                                                .fullName(customer.getFullName())
                                                .phone(customer.getPhone())
                                                .roles(java.util.Collections.emptySet())
                                                .isActive(customer.getIsActive())
                                                .createdAt(customer.getCreatedAt())
                                                .build();
                        }

                        @Transactional
                        public UserResponse adminUpdateCustomer(Long customerId, CustomerUpdateRequest request) {
                                Customer customer = customerRepository.findById(customerId)
                                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                                if (Boolean.FALSE.equals(customer.getIsActive())) {
                                        throw new IllegalStateException("Customer đã bị vô hiệu hóa");
                                }
                                boolean changed = false;
                                if (request.getFullName() != null) { customer.setFullName(request.getFullName()); changed = true; }
                                if (request.getPhone() != null) { customer.setPhone(request.getPhone()); changed = true; }
                                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                                        customer.setPasswordHash(passwordEncoder.encode(request.getPassword())); changed = true; }
                                if (changed) {
                                        customer.setUpdatedAt(LocalDateTime.now());
                                        customerRepository.save(customer);
                                }
                                return UserResponse.builder()
                                                .id(customer.getId())
                                                .email(customer.getEmail())
                                                .fullName(customer.getFullName())
                                                .phone(customer.getPhone())
                                                .roles(java.util.Collections.emptySet())
                                                .isActive(customer.getIsActive())
                                                .createdAt(customer.getCreatedAt())
                                                .build();
                        }
}

