package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.OrderResponse;
import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.UpdateOrderRequest;
import com.fashion.leon.fashionshopbackend.dto.UpdateOrderStatusRequest;
import com.fashion.leon.fashionshopbackend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Admin Orders", description = "Admin order management APIs")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STAFF', 'MANAGER')")
    @Operation(summary = "Get order by ID for admin", description = "Retrieve order details by ID for admins")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.getOrderDetailForAdmin(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STAFF', 'MANAGER')")
    @Operation(summary = "Get all orders", description = "Retrieve all orders with pagination for admins")
    public ResponseEntity<PaginatedResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(name = "search", required = false) String search) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("ASC")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Page<OrderResponse> orderPage = orderService.getAllOrders(pageable, search);

            PaginatedResponse<OrderResponse> response = new PaginatedResponse<>(
                    page,
                    size,
                    orderPage.getTotalElements(),
                    orderPage.getTotalPages(),
                    orderPage.isLast(),
                    orderPage.getContent()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    @Operation(summary = "Update order details", description = "Update shipping address and item quantities for an order")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        try {
            OrderResponse response = orderService.updateOrder(orderId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STAFF', 'MANAGER')")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
