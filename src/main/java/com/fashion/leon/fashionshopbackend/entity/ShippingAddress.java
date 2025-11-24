package com.fashion.leon.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipping_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String address;
    private String phone;
    private String city;
    private String zip;
    private String country;
}
