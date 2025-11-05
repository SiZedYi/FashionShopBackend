package com.fashion.leon.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "full_name")
    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    @Column(name = "postal_code")
    private String postalCode;
    private String country;
    @Column(name = "is_default")
    private Boolean isDefault;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}