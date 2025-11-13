package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.PaginatedResponse;
import com.fashion.leon.fashionshopbackend.dto.RoleRequest;
import com.fashion.leon.fashionshopbackend.dto.RoleResponse;
import com.fashion.leon.fashionshopbackend.entity.Permission;
import com.fashion.leon.fashionshopbackend.entity.Role;
import com.fashion.leon.fashionshopbackend.exception.ResourceNotFoundException;
import com.fashion.leon.fashionshopbackend.repository.PermissionRepository;
import com.fashion.leon.fashionshopbackend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PaginatedResponse<RoleResponse> list(Pageable pageable) {
        Page<Role> page = roleRepository.findAll(pageable);
        List<RoleResponse> data = page.getContent().stream()
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

    public RoleResponse get(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return toResponse(role);
    }

    public RoleResponse create(RoleRequest request) {
        String name = request.getName().trim().toLowerCase();
        roleRepository.findByName(name).ifPresent(existing -> {
            throw new IllegalArgumentException("Role already exists: " + name);
        });
        Role role = Role.builder()
                .name(name)
                .description(request.getDescription())
                .build();

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            if (perms.size() != request.getPermissionIds().size()) {
                throw new ResourceNotFoundException("One or more permissions not found");
            }
            role.setPermissions(perms);
        }
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        String name = request.getName().trim().toLowerCase();
        roleRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Role already exists: " + name);
            }
        });
        role.setName(name);
        role.setDescription(request.getDescription());
        if (request.getPermissionIds() != null) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            if (perms.size() != request.getPermissionIds().size()) {
                throw new ResourceNotFoundException("One or more permissions not found");
            }
            role.setPermissions(perms);
        }
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        roleRepository.delete(role);
    }

    private RoleResponse toResponse(Role role) {
        List<String> perms = role.getPermissions() == null ? List.of() : role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .collect(Collectors.toList());
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(perms)
                .build();
    }
}
