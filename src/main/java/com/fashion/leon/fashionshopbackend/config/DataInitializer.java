package com.fashion.leon.fashionshopbackend.config;

import com.fashion.leon.fashionshopbackend.entity.Role;
import com.fashion.leon.fashionshopbackend.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
	private final RoleRepository roleRepository;

	@PostConstruct
	public void ensureDefaultRolesExist() {
		ensureRole("superadmin", "Super administrator role with all privileges");
		ensureRole("admin", "Administrator role");
		ensureRole("manager", "Manager role");
		ensureRole("staff", "Staff member role");
	}

	private void ensureRole(String name, String description) {
		roleRepository.findByName(name.toLowerCase())
				.orElseGet(() -> {
					log.info("Seeding role: {}", name);
					return roleRepository.save(Role.builder().name(name.toLowerCase()).description(description).build());
				});
	}
}
