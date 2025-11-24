package com.fashion.leon.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"orderItems"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_number")
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private BigDecimal subtotal;

    private BigDecimal tax;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus; // "PENDING", "PAID", "FAILED", "REFUNDED"

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;

    @Column(name = "shipping_address_snapshot")
    private String shippingAddressSnapshot;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    public enum Status {
        pending, paid, shipped, cancelled, refunded
    }
}
