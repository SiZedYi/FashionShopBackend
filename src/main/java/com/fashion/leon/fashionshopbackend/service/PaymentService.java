package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.ConfirmPaymentResponse;
import com.fashion.leon.fashionshopbackend.dto.PaymentIntentResponse;
import com.fashion.leon.fashionshopbackend.entity.Order;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final StripeService stripeService;
    private final OrderService orderService;
    private final EmailService emailService;

    /**
     * Create Stripe Payment Intent
     */
    public PaymentIntentResponse createPaymentIntent(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Calculate amount in cents
        long amountInCents = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        // Create payment intent params
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .putMetadata("orderId", order.getId().toString())
                .putMetadata("orderNumber", order.getOrderNumber())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        // Create payment intent using StripeClient
        PaymentIntent paymentIntent = stripeService.getStripeClient().paymentIntents().create(params);

        log.info("Created payment intent {} for order {}", paymentIntent.getId(), order.getOrderNumber());

        return PaymentIntentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .orderId(order.getId())
                .amount(amountInCents)
                .build();
    }

    /**
     * Confirm payment
     */
    @Transactional
    public ConfirmPaymentResponse confirmPayment(Long orderId, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        try {
            // Retrieve payment intent to verify status
            PaymentIntent paymentIntent = stripeService.getStripeClient()
                    .paymentIntents()
                    .retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Update order payment status
                orderService.updatePaymentStatus(orderId, "PAID", paymentIntentId);

                log.info("Payment confirmed for order {}", order.getOrderNumber());

                // Send order confirmation email
                try {
                    Order updatedOrder = orderRepository.findByIdWithItems(orderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                    emailService.sendOrderConfirmationEmail(updatedOrder);
                } catch (Exception e) {
                    log.error("Failed to send order confirmation email for order {}", order.getOrderNumber(), e);
                    // Continue even if email fails
                }

                return ConfirmPaymentResponse.builder()
                        .success(true)
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .message("Payment confirmed successfully")
                        .build();
            } else {
                log.warn("Payment intent {} status is {}", paymentIntentId, paymentIntent.getStatus());

                return ConfirmPaymentResponse.builder()
                        .success(false)
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .message("Payment not completed. Status: " + paymentIntent.getStatus())
                        .build();
            }
        } catch (StripeException e) {
            log.error("Error confirming payment for order {}: {}", order.getOrderNumber(), e.getMessage(), e);

            // Update payment status to failed
            orderService.updatePaymentStatus(orderId, "FAILED", paymentIntentId);

            return ConfirmPaymentResponse.builder()
                    .success(false)
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .message("Payment confirmation failed: " + e.getMessage())
                    .build();
        }
    }
}
