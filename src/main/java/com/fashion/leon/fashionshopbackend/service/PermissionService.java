package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.PermissionRequest;
import com.fashion.leon.fashionshopbackend.dto.PermissionResponse;
import com.fashion.leon.fashionshopbackend.entity.Permission;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PaginatedResponse<PermissionResponse> list(Pageable pageable) {
        Page<Permission> page = permissionRepository.findAll(pageable);
        List<PermissionResponse> data = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    return new PaginatedResponse<>(
        page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                data
        );
    }

    public PermissionResponse get(Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        return toResponse(p);
    }

    public PermissionResponse create(PermissionRequest request) {
        String name = request.getName().trim().toUpperCase();
        permissionRepository.findByName(name).ifPresent(existing -> {
            throw new IllegalArgumentException("Permission already exists: " + name);
        });
        Permission p = Permission.builder()
                .name(name)
                .description(request.getDescription())
                .build();
        return toResponse(permissionRepository.save(p));
    }

    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        String name = request.getName().trim().toUpperCase();
        permissionRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Permission already exists: " + name);
            }
        });
        p.setName(name);
        p.setDescription(request.getDescription());
        return toResponse(permissionRepository.save(p));
    }

    public void delete(Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        permissionRepository.delete(p);
    }

    private PermissionResponse toResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
