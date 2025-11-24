package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.entity.Order;
import com.fashion.leon.fashionshopbackend.entity.OrderItem;
import com.fashion.leon.fashionshopbackend.entity.ShippingAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Fashion Shop!");
            message.setText(buildWelcomeEmailContent(fullName));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception to avoid breaking registration process
        }
    }

    private String buildWelcomeEmailContent(String fullName) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to Fashion Shop!\n\n" +
            "Your account has been successfully created. You can start shopping for our amazing fashion products.\n\n" +
            "Thank you for trusting and choosing Fashion Shop!\n\n" +
            "Best regards,\n" +
            "Fashion Shop Team",
            fullName
        );
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset Password - Fashion Shop");
            message.setText(buildPasswordResetEmailContent(resetToken));
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailContent(String resetToken) {
        return String.format(
            "Hi,\n\n" +
            "You have requested to reset the password for your Fashion Shop account.\n\n" +
            "Your password reset token is: %s\n\n" +
            "Please use this token to reset your password. This token will expire in 15 minutes.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Fashion Shop Team",
            resetToken
        );
    }

    /**
     * Send order confirmation email when payment is successful
     */
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            if (order == null || order.getCustomer() == null || order.getCustomer().getEmail() == null) {
                log.warn("Cannot send order confirmation email: order or customer email is null");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("Order Confirmation - " + order.getOrderNumber());
            message.setText(buildOrderConfirmationEmailContent(order));

            mailSender.send(message);
            log.info("Order confirmation email sent successfully to: {} for order: {}", 
                    order.getCustomer().getEmail(), order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", 
                    order.getOrderNumber(), e);
            // Don't throw exception to avoid breaking payment process
        }
    }

    private String buildOrderConfirmationEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Header
        content.append("Dear ").append(order.getCustomer().getFullName()).append(",\n\n");
        content.append("Thank you for your order! We're pleased to confirm that we have received your order.\n\n");
        
        // Order Information
        content.append("═══════════════════════════════════════════════\n");
        content.append("ORDER DETAILS\n");
        content.append("═══════════════════════════════════════════════\n\n");
        
        content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        content.append("Order Date: ").append(order.getPlacedAt() != null ? 
                order.getPlacedAt().format(dateFormatter) : "N/A").append("\n");
        content.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        content.append("Payment Status: ").append(order.getPaymentStatus()).append("\n");
        
        if (order.getPaidAt() != null) {
            content.append("Paid At: ").append(order.getPaidAt().format(dateFormatter)).append("\n");
        }
        
        content.append("\n");

        // Shipping Address
        if (order.getShippingAddress() != null) {
            ShippingAddress addr = order.getShippingAddress();
            content.append("SHIPPING ADDRESS:\n");
            content.append("─────────────────────────────────────────────\n");
            content.append(addr.getFirstName()).append(" ").append(addr.getLastName()).append("\n");
            content.append(addr.getAddress()).append("\n");
            content.append(addr.getCity()).append(", ").append(addr.getZip()).append("\n");
            content.append(addr.getCountry()).append("\n");
            content.append("Phone: ").append(addr.getPhone()).append("\n\n");
        }

        // Order Items
        content.append("ORDER ITEMS:\n");
        content.append("─────────────────────────────────────────────\n");
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                content.append(String.format("• %s", item.getProductName() != null ? 
                        item.getProductName() : "Product")).append("\n");
                
                if (item.getColor() != null && !item.getColor().isEmpty()) {
                    content.append("  Color: ").append(item.getColor()).append("\n");
                }
                
                content.append(String.format("  Quantity: %d x %s = %s\n",
                        item.getQuantity(),
                        currencyFormatter.format(item.getUnitPrice()),
                        currencyFormatter.format(item.getLineTotal())
                ));
                content.append("\n");
            }
        }

        // Order Summary
        content.append("─────────────────────────────────────────────\n");
        content.append("ORDER SUMMARY:\n");
        content.append("─────────────────────────────────────────────\n");
        
        if (order.getSubtotal() != null) {
            content.append(String.format("Subtotal:        %s\n", 
                    currencyFormatter.format(order.getSubtotal())));
        }
        
        if (order.getTax() != null) {
            content.append(String.format("Tax:             %s\n", 
                    currencyFormatter.format(order.getTax())));
        }
        
        if (order.getShippingFee() != null) {
            content.append(String.format("Shipping Fee:    %s\n", 
                    currencyFormatter.format(order.getShippingFee())));
        }
        
        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
            content.append(String.format("Coupon Code:     %s\n", order.getCouponCode()));
        }
        
        content.append(String.format("\nTotal Amount:    %s\n", 
                currencyFormatter.format(order.getTotalAmount())));
        
        content.append("═══════════════════════════════════════════════\n\n");

        // Footer
        content.append("We will notify you once your order has been shipped.\n\n");
        content.append("If you have any questions about your order, please don't hesitate to contact us.\n\n");
        content.append("Thank you for shopping with Fashion Shop!\n\n");
        content.append("Best regards,\n");
        content.append("Fashion Shop Team\n\n");
        content.append("---\n");
        content.append("This is an automated email. Please do not reply to this message.\n");

        return content.toString();
    }
}