package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.*;
import com.fashion.leon.fashionshopbackend.entity.*;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    /**
     * Create new order
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String email) {
        // Get customer by email
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        // Create shipping address
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .firstName(request.getShippingAddress().getFirstName())
                .lastName(request.getShippingAddress().getLastName())
                .address(request.getShippingAddress().getAddress())
                .phone(request.getShippingAddress().getPhone())
                .city(request.getShippingAddress().getCity())
                .zip(request.getShippingAddress().getZip())
                .country(request.getShippingAddress().getCountry())
                .build();

        // Determine payment status based on payment method
        String paymentStatus = "COD".equals(request.getPaymentMethod()) ? "PENDING" : "PENDING";

        // Create order
        Order order = Order.builder()
                .customer(customer)
                .orderNumber(generateOrderNumber())
                .status(Order.Status.pending)
                .paymentStatus(paymentStatus)
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .shippingFee(request.getShippingFee())
                .couponCode(request.getCouponCode())
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(shippingAddress)
                .shippingAddressSnapshot(formatShippingAddress(request.getShippingAddress()))
                .placedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        // Create order items
        for (OrderItemDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            // Check stock
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getPrice())
                    .lineTotal(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())))
                    .color(itemDto.getColor())
                    .productName(itemDto.getProductName())
                    .productImage(itemDto.getProductImage())
                    .build();

            orderItemRepository.save(orderItem);
        }

        log.info("Created order {} for customer {}", order.getOrderNumber(), email);

        // Reload order with items
        order = orderRepository.findByIdWithItems(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Send order confirmation email for COD orders
        if ("COD".equals(request.getPaymentMethod())) {
            try {
                emailService.sendOrderConfirmationEmail(order);
            } catch (Exception e) {
                log.error("Failed to send order confirmation email for COD order {}", order.getOrderNumber(), e);
                // Continue even if email fails
            }
        }

        return mapToOrderResponse(order);
    }

    /**
     * Get order by ID
     */
    public OrderResponse getOrderById(Long id, String email) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Verify order belongs to customer
        if (!order.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        return mapToOrderResponse(order);
    }

    /**
     * Get all orders for a customer
     */
    public List<OrderResponse> getMyOrders(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        List<Order> orders = orderRepository.findByCustomerId(customer.getId(), Pageable.unpaged()).getContent();
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get customer orders with pagination
     */
    public Page<OrderResponse> getCustomerOrders(String email, Pageable pageable) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        Page<Order> orders = orderRepository.findByCustomerId(customer.getId(), pageable);
        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Get all orders for admin with optional search by orderNumber, id, or status
     */
    public Page<OrderResponse> getAllOrders(Pageable pageable, String search) {
        Page<Order> orders;

        if (search == null || search.trim().isEmpty()) {
            orders = orderRepository.findAll(pageable);
        } else {
            String q = search.trim();

            // Try parse numeric id
            Long id = null;
            try {
                id = Long.parseLong(q);
            } catch (NumberFormatException ignored) {}

            // Try parse status enum (case-insensitive)
            Order.Status status = null;
            for (Order.Status s : Order.Status.values()) {
                if (s.name().equalsIgnoreCase(q)) {
                    status = s;
                    break;
                }
            }

            orders = orderRepository.searchOrders(id, q, status, pageable);
        }

        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Get order detail for admin
     */
    public OrderResponse getOrderDetailForAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    /**
     * Update order for admin
     */
    @Transactional
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Update shipping address if provided
        if (request.getShippingAddress() != null) {
            ShippingAddressRequest addressReq = request.getShippingAddress();
            ShippingAddress shippingAddress = order.getShippingAddress();
            if (shippingAddress == null) {
                shippingAddress = new ShippingAddress();
                order.setShippingAddress(shippingAddress);
            }
            shippingAddress.setFirstName(addressReq.getFirstName());
            shippingAddress.setLastName(addressReq.getLastName());
            shippingAddress.setAddress(addressReq.getAddress());
            shippingAddress.setCity(addressReq.getCity());
            shippingAddress.setZip(addressReq.getZip());
            shippingAddress.setCountry(addressReq.getCountry());
            shippingAddress.setPhone(addressReq.getPhone());
            order.setShippingAddressSnapshot(formatShippingAddress(addressReq));
        }

        BigDecimal newSubtotal = BigDecimal.ZERO;

        // Update order items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (UpdateOrderItemRequest itemReq : request.getItems()) {
                OrderItem orderItem = order.getOrderItems().stream()
                        .filter(oi -> oi.getId().equals(itemReq.getItemId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + itemReq.getItemId()));

                Product product = orderItem.getProduct();
                int oldQuantity = orderItem.getQuantity();
                int newQuantity = itemReq.getQuantity();

                if (newQuantity <= 0) {
                    // For simplicity, we don't allow removing items, just changing quantity.
                    // To remove, one might need a separate endpoint or handle quantity 0 as deletion.
                    throw new IllegalArgumentException("Quantity must be positive.");
                }

                int quantityChange = newQuantity - oldQuantity;

                if (quantityChange > 0 && product.getStockQuantity() < quantityChange) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }

                // Adjust stock
                product.setStockQuantity(product.getStockQuantity() - quantityChange);
                productRepository.save(product);

                // Update order item
                orderItem.setQuantity(newQuantity);
                orderItem.setLineTotal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
                orderItemRepository.save(orderItem);
            }
        }

        // Recalculate totals
        for (OrderItem item : order.getOrderItems()) {
            newSubtotal = newSubtotal.add(item.getLineTotal());
        }

        order.setSubtotal(newSubtotal);
        // Assuming tax and shipping are fixed for this update logic.
        // A more complex scenario might require recalculating these as well.
        BigDecimal totalAmount = newSubtotal.add(order.getTax()).add(order.getShippingFee());
        order.setTotalAmount(totalAmount);

        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        log.info("Updated order {} by admin", updatedOrder.getOrderNumber());

        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Cancel order
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Verify order belongs to customer
        if (!order.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        // Check if order can be cancelled
        if (order.getStatus() == Order.Status.shipped || order.getStatus() == Order.Status.cancelled) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        // Update order status
        order.setStatus(Order.Status.cancelled);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Restore stock if payment was completed
        if ("PAID".equals(order.getPaymentStatus()) && order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        log.info("Cancelled order {} for customer {}", order.getOrderNumber(), email);

        return mapToOrderResponse(order);
    }

    /**
     * Update order status for admin
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        try {
            Order.Status newStatus = Order.Status.valueOf(status.toLowerCase());
            order.setStatus(newStatus);

            // If order is refunded, restore stock
            if (newStatus == Order.Status.refunded && "PAID".equals(order.getPaymentStatus())) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        Product product = item.getProduct();
                        if (product != null) {
                            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                            productRepository.save(product);
                        }
                    }
                }
                order.setPaymentStatus("REFUNDED");
            }

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.info("Updated order {} status to {}", order.getOrderNumber(), status);

            return mapToOrderResponse(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    /**
     * Update payment status (used by PaymentService)
     */
    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus, String paymentIntentId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order.setStripePaymentIntentId(paymentIntentId);
        
        if ("PAID".equals(paymentStatus)) {
            order.setStatus(Order.Status.paid);
            order.setPaidAt(LocalDateTime.now());

            // Reduce product stock
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Updated order {} payment status to {}", order.getOrderNumber(), paymentStatus);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Format shipping address to string
     */
    private String formatShippingAddress(ShippingAddressRequest address) {
        return String.format("%s %s\n%s\n%s, %s\n%s\nPhone: %s",
                address.getFirstName(),
                address.getLastName(),
                address.getAddress(),
                address.getCity(),
                address.getZip(),
                address.getCountry(),
                address.getPhone());
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = java.util.Collections.emptyList();
        
        if (order.getOrderItems() != null) {
            orderItemResponses = order.getOrderItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProductName())
                            .productSku(item.getProduct().getSku())
                            .productImage(item.getProductImage())  // Use snapshot stored in OrderItem
                            .color(item.getColor())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .lineTotal(item.getLineTotal())
                            .build())
                    .collect(Collectors.toList());
        }

        ShippingAddressResponse shippingAddressResponse = null;
        if (order.getShippingAddress() != null) {
            shippingAddressResponse = ShippingAddressResponse.builder()
                    .firstName(order.getShippingAddress().getFirstName())
                    .lastName(order.getShippingAddress().getLastName())
                    .address(order.getShippingAddress().getAddress())
                    .phone(order.getShippingAddress().getPhone())
                    .city(order.getShippingAddress().getCity())
                    .zip(order.getShippingAddress().getZip())
                    .country(order.getShippingAddress().getCountry())
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingFee(order.getShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .couponCode(order.getCouponCode())
                .shippingAddress(shippingAddressResponse)
                .placedAt(order.getPlacedAt())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(orderItemResponses)
                .build();
    }
}
