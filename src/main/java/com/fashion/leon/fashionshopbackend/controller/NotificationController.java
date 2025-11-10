package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.NotificationResponse;
import com.fashion.leon.fashionshopbackend.entity.Notification;
import com.fashion.leon.fashionshopbackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPERADMIN')")
    public ResponseEntity<Page<NotificationResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        List<Notification> list = notificationRepository.findByOrderByIdDesc(pageable);
        long total = notificationRepository.count();
        List<NotificationResponse> mapped = list.stream().map(this::toDto).toList();
        Page<NotificationResponse> resp = new PageImpl<>(mapped, pageable, total);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPERADMIN')")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(n -> ResponseEntity.ok(toDto(n)))
                .orElse(ResponseEntity.notFound().build());
    }

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .payload(n.getPayload())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
