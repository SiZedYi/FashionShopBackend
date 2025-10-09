package com.fashion.leon.fashionshopbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}