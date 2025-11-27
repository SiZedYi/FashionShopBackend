package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.*;
import com.fashion.leon.fashionshopbackend.entity.Address;
import com.fashion.leon.fashionshopbackend.entity.Customer;
import com.fashion.leon.fashionshopbackend.exception.EmailAlreadyExistsException;
import com.fashion.leon.fashionshopbackend.exception.InvalidCredentialsException;
import com.fashion.leon.fashionshopbackend.repository.AddressRepository;
import com.fashion.leon.fashionshopbackend.repository.CustomerRepository;
import com.fashion.leon.fashionshopbackend.repository.OrderRepository;
import com.fashion.leon.fashionshopbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
    private final AddressRepository addressRepository;

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

        // Generate JWT token with customer info (no roles/permissions for customers)
        String token = jwtUtil.generateToken(
                savedCustomer.getEmail(),
                savedCustomer.getFullName(),
                savedCustomer.getPhone(),
                Collections.emptySet(),
                Collections.emptySet()
        );

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
                .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList())
                .build();

        // Generate JWT token with customer info (no roles/permissions for customers)
        String token = jwtUtil.generateToken(
                customer.getEmail(),
                customer.getFullName(),
                customer.getPhone(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .message("Đăng nhập thành công!")
                .build();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrueFetchAddresses(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        log.info("Fetching profile for customer with email: {}", customer.getEmail());

        java.util.List<AddressResponse> addressResponses = customer.getAddresses() != null ?
                customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList();

        return UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .addresses(addressResponses)
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
                                                        .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                                                                .id(a.getId())
                                                                .fullName(a.getFullName())
                                                                .phone(a.getPhone())
                                                                .line1(a.getLine1())
                                                                .line2(a.getLine2())
                                                                .city(a.getCity())
                                                                .state(a.getState())
                                                                .postalCode(a.getPostalCode())
                                                                .country(a.getCountry())
                                                                .isDefault(a.getIsDefault())
                                                                .createdAt(a.getCreatedAt())
                                                                .updatedAt(a.getUpdatedAt())
                                                                .build()).toList() : java.util.Collections.emptyList())
                                                        .build();
                        }

                        @Transactional
                        public UserResponse adminUpdateCustomer(Long customerId, CustomerUpdateRequest request) {
                                Customer customer = customerRepository.findById(customerId)
                                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                                boolean changed = false;
                                if (request.getFullName() != null) { customer.setFullName(request.getFullName()); changed = true; }
                                if (request.getPhone() != null) { customer.setPhone(request.getPhone()); changed = true; }
                                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                                        customer.setPasswordHash(passwordEncoder.encode(request.getPassword())); changed = true; }
                                if (request.getIsActive() != null) { customer.setIsActive(request.getIsActive()); changed = true; }
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
                                                        .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                                                                .id(a.getId())
                                                                .fullName(a.getFullName())
                                                                .phone(a.getPhone())
                                                                .line1(a.getLine1())
                                                                .line2(a.getLine2())
                                                                .city(a.getCity())
                                                                .state(a.getState())
                                                                .postalCode(a.getPostalCode())
                                                                .country(a.getCountry())
                                                                .isDefault(a.getIsDefault())
                                                                .createdAt(a.getCreatedAt())
                                                                .updatedAt(a.getUpdatedAt())
                                                                .build()).toList() : java.util.Collections.emptyList())
                                                        .build();
                        }

                        public List<UserResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(customer -> UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(java.util.Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList())
                .build()).toList();
    }

    public UserResponse getCustomerDetail(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(java.util.Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList())
                .build();
    }

    @Transactional
    public UserResponse addAddress(String email, AddressRequest request) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        boolean setAsDefault = request.getIsDefault() != null ? request.getIsDefault() : false;

        // If setting as default, remove default from other addresses
        if (setAsDefault) {
            List<Address> customerAddresses = addressRepository.findByCustomerId(customer.getId());
            for (Address addr : customerAddresses) {
                if (Boolean.TRUE.equals(addr.getIsDefault())) {
                    addr.setIsDefault(false);
                    addr.setUpdatedAt(LocalDateTime.now());
                    addressRepository.save(addr);
                }
            }
        }

        Address address = Address.builder()
                .customer(customer)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(setAsDefault)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (customer.getAddresses() == null) {
            customer.setAddresses(new java.util.ArrayList<>());
        }
        customer.getAddresses().add(address);
        customerRepository.save(customer);

        return getCustomerProfile(email);
    }

    public List<AddressResponse> getAddresses(String email) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrueFetchAddresses(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer.getAddresses() == null) return java.util.Collections.emptyList();
        return customer.getAddresses().stream().map(a -> AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .isDefault(a.getIsDefault())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build()).toList();
    }

    public AddressResponse getAddressDetail(String email, Long addressId) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Address does not belong to customer");
        }

        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        Customer customer = customerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Address does not belong to customer");
        }

        // If setting as default, remove default from other addresses
        if (request.getIsDefault() != null && request.getIsDefault()) {
            List<Address> customerAddresses = addressRepository.findByCustomerId(customer.getId());
            for (Address addr : customerAddresses) {
                if (!addr.getId().equals(addressId) && Boolean.TRUE.equals(addr.getIsDefault())) {
                    addr.setIsDefault(false);
                    addr.setUpdatedAt(LocalDateTime.now());
                    addressRepository.save(addr);
                }
            }
        }

        // Update address fields
        if (request.getFullName() != null) address.setFullName(request.getFullName());
        if (request.getPhone() != null) address.setPhone(request.getPhone());
        if (request.getLine1() != null) address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        address.setState(request.getState());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());
        address.setUpdatedAt(LocalDateTime.now());

        Address updatedAddress = addressRepository.save(address);

        return AddressResponse.builder()
                .id(updatedAddress.getId())
                .fullName(updatedAddress.getFullName())
                .phone(updatedAddress.getPhone())
                .line1(updatedAddress.getLine1())
                .line2(updatedAddress.getLine2())
                .city(updatedAddress.getCity())
                .state(updatedAddress.getState())
                .postalCode(updatedAddress.getPostalCode())
                .country(updatedAddress.getCountry())
                .isDefault(updatedAddress.getIsDefault())
                .createdAt(updatedAddress.getCreatedAt())
                .updatedAt(updatedAddress.getUpdatedAt())
                .build();
    }

    public PaginatedResponse<UserResponse> getAllCustomersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        List<UserResponse> content = customerPage.getContent().stream().map(customer -> UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(java.util.Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList())
                .build()).toList();
        return new PaginatedResponse<>(
                page,
                size,
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast(),
                content
        );
    }

    public PaginatedResponse<UserResponse> getAllCustomersPaged(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Customer> customerPage = customerRepository.findByFullNameContainingIgnoreCase(name, pageable);
        List<UserResponse> content = customerPage.getContent().stream().map(customer -> UserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .roles(java.util.Collections.emptySet())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .addresses(customer.getAddresses() != null ? customer.getAddresses().stream().map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .line1(a.getLine1())
                        .line2(a.getLine2())
                        .city(a.getCity())
                        .state(a.getState())
                        .postalCode(a.getPostalCode())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList() : java.util.Collections.emptyList())
                .build()).toList();
        return new PaginatedResponse<>(
                page,
                size,
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast(),
                content
        );
    }
}

