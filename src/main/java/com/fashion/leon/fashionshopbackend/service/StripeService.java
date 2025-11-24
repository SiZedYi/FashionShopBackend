package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.entity.Order;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${frontend.payment.success.url}")
    private String successUrl;

    @Value("${frontend.payment.cancel.url}")
    private String cancelUrl;

    private StripeClient stripeClient;

    @PostConstruct
    public void init() {
        this.stripeClient = new StripeClient(stripeSecretKey);
        log.info("Stripe client initialized successfully");
    }

    /**
     * Get StripeClient instance
     */
    public StripeClient getStripeClient() {
        return stripeClient;
    }

    /**
     * Create a Stripe Checkout Session
     */
    public Session createCheckoutSession(Order order, List<SessionCreateParams.LineItem> lineItems) throws StripeException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("order_id", order.getId().toString());
        metadata.put("order_number", order.getOrderNumber());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .addAllLineItem(lineItems)
                .putAllMetadata(metadata)
                .setCustomerEmail(order.getCustomer().getEmail())
                .build();

        return stripeClient.checkout().sessions().create(params);
    }

    /**
     * Create line items for Stripe Checkout
     */
    public SessionCreateParams.LineItem createLineItem(String name, BigDecimal price, Long quantity) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(price.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(name)
                                                .build()
                                )
                                .build()
                )
                .setQuantity(quantity)
                .build();
    }

    /**
     * Verify Stripe webhook signature
     */
    public Event constructEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }

    /**
     * Retrieve a Checkout Session by ID
     */
    public Session retrieveSession(String sessionId) throws StripeException {
        return stripeClient.checkout().sessions().retrieve(sessionId);
    }
}
