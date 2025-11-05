package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.AuthResponse;
import com.fashion.leon.fashionshopbackend.dto.LoginRequest;
import com.fashion.leon.fashionshopbackend.dto.RegisterRequest;
import com.fashion.leon.fashionshopbackend.dto.UserResponse;
import com.fashion.leon.fashionshopbackend.entity.Role;
import com.fashion.leon.fashionshopbackend.entity.User;
import com.fashion.leon.fashionshopbackend.exception.EmailAlreadyExistsException;
import com.fashion.leon.fashionshopbackend.exception.InvalidCredentialsException;
import com.fashion.leon.fashionshopbackend.repository.RoleRepository;
import com.fashion.leon.fashionshopbackend.repository.UserRepository;
import com.fashion.leon.fashionshopbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        log.info("Attempting to register admin user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + request.getEmail());
        }

        // Fetch the 'admin' role
        Role adminRole = roleRepository.findByName("admin")
                .orElseThrow(() -> new RuntimeException("Default role 'admin' not found in database"));

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .roles(new HashSet<>(Set.of(adminRole)))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("Admin user registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .roles(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .build();

        // Send welcome email asynchronously (won't block registration if email fails)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng ký tài khoản admin thành công! Email chào mừng đang được gửi.")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest request) {
        log.info("Attempting to login admin user with email: {}", request.getEmail());

        // Find user by email and FETCH roles eagerly
        User user = userRepository.findByEmailAndIsActiveTrueFetchRoles(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

        // Force initialize the roles collection within transaction
        user.getRoles().size(); // This triggers Hibernate to load the collection

        // DEBUG: Check if roles collection is null or empty
        log.info("DEBUG - User founds: {}", user.getEmail());
        log.info("DEBUG - Roles collection is null? {}", user.getRoles() == null);
        log.info("DEBUG - Roles collection size: {}", user.getRoles() != null ? user.getRoles().size() : "NULL");

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            log.info("DEBUG - Roles details:");
            user.getRoles().forEach(role -> log.info("  - Role ID: {}, Name: {}", role.getId(), role.getName()));
        }

        // Check for null password hash
        if (user.getPasswordHash() == null) {
            log.error("User with email {} has null password hash", request.getEmail());
            throw new InvalidCredentialsException("Tài khoản chưa thiết lập mật khẩu hoặc dữ liệu không hợp lệ");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }

        log.info("Admin user login successful for email: {}", request.getEmail());

        // Handle empty or null roles collection
        Set<String> roleNames;
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.warn("User {} has no roles assigned!", user.getEmail());
            roleNames = new HashSet<>();
        } else {
            roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        }
        log.info("Roles: {}", roleNames);

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(roleNames)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng nhập thành công!")
                .build();
    }

    public UserResponse getUserProfile(String email) {
        // Use FETCH method to eagerly load roles
        User user = userRepository.findByEmailAndIsActiveTrueFetchRoles(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching profile for admin user with email: {}", user.getEmail());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
