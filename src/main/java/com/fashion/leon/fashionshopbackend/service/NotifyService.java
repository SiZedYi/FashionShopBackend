package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.entity.Notification;
import com.fashion.leon.fashionshopbackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service to create Notification records and push webhook events to external systems (e.g. Slack, Discord, custom).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyService {

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${webhook.admin.url:}")
    private String adminWebhookUrl;

    @Transactional
    public Notification notify(String type, String title, String message, Map<String, Object> payload) {
        String payloadJson = toJson(payload);
        Notification notification = Notification.builder()
                .type(type)
                .title(title)
                .message(message)
                .payload(payloadJson)
                .createdAt(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(notification);
        // Fire webhook (best-effort, non-blocking failure)
        pushWebhookAsync(saved);
        return saved;
    }

    // Convenience helpers
    @Transactional
    public Notification notifyUserCreated(String email, String fullName, Object roles) {
    return notify(
        "user_created",
        "User mới được tạo",
        "User '" + fullName + "' (" + email + ") đã được tạo.",
        java.util.Map.of(
            "email", email,
            "fullName", fullName,
            "roles", roles
        )
    );
    }

    @Transactional
    public Notification notifyOrderCreated(Long orderId, String orderNumber, Number totalAmount, String customerEmail) {
    return notify(
        "order_created",
        "Đơn hàng mới",
        "Đơn hàng " + orderNumber + " đã được tạo.",
        java.util.Map.of(
            "orderId", orderId,
            "orderNumber", orderNumber,
            "totalAmount", totalAmount,
            "customerEmail", customerEmail
        )
    );
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) return "{}";
        // Simple manual JSON build to avoid adding new dependency; for complex use Jackson ObjectMapper.
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : payload.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void pushWebhookAsync(Notification notification) {
        if (adminWebhookUrl == null || adminWebhookUrl.isBlank()) {
            log.debug("Webhook URL not configured; skipping push for notification id={}", notification.getId());
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{" +
                    "\"type\":\"" + notification.getType() + "\"," +
                    "\"title\":\"" + (notification.getTitle() == null ? "" : notification.getTitle()) + "\"," +
                    "\"message\":\"" + (notification.getMessage() == null ? "" : notification.getMessage()) + "\"," +
                    "\"payload\":" + (notification.getPayload() == null ? "{}" : notification.getPayload()) + 
                    "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(adminWebhookUrl, entity, String.class);
            log.info("Webhook pushed for notification id={} type={}", notification.getId(), notification.getType());
        } catch (RestClientException ex) {
            log.error("Failed to push webhook for notification id={} : {}", notification.getId(), ex.getMessage());
        }
    }
}
