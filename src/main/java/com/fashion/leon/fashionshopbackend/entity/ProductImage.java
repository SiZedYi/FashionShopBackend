package com.fashion.leon.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String url;
    @Column(name = "alt_text")
    private String altText;
    @Column(name = "is_primary")
    private Boolean isPrimary;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

