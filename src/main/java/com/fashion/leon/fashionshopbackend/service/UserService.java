package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.AuthResponse;
import com.fashion.leon.fashionshopbackend.dto.LoginRequest;
import com.fashion.leon.fashionshopbackend.dto.RegisterRequest;
import com.fashion.leon.fashionshopbackend.dto.UserResponse;
import com.fashion.leon.fashionshopbackend.entity.User;
import com.fashion.leon.fashionshopbackend.exception.EmailAlreadyExistsException;
import com.fashion.leon.fashionshopbackend.exception.InvalidCredentialsException;
import com.fashion.leon.fashionshopbackend.repository.UserRepository;
import com.fashion.leon.fashionshopbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.customer)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .build();

        // Send welcome email asynchronously (won't block registration if email fails)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng ký tài khoản thành công! Email chào mừng đang được gửi.")
                .build();
    }

    public AuthResponse loginUser(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }

        log.info("User login successful for email: {}", request.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Create user response
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng nhập thành công!")
                .build();
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}