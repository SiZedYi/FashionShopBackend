package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.ConfirmPaymentRequest;
import com.fashion.leon.fashionshopbackend.dto.ConfirmPaymentResponse;
import com.fashion.leon.fashionshopbackend.dto.PaymentIntentRequest;
import com.fashion.leon.fashionshopbackend.dto.PaymentIntentResponse;
import com.fashion.leon.fashionshopbackend.service.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Payments", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/stripe/create-payment-intent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create Stripe payment intent", description = "Create a payment intent for an order")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            PaymentIntentResponse response = paymentService.createPaymentIntent(request.getOrderId());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/stripe/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm payment", description = "Confirm a payment and update order status")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            ConfirmPaymentResponse response = paymentService.confirmPayment(
                    request.getOrderId(),
                    request.getPaymentIntentId()
            );

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
