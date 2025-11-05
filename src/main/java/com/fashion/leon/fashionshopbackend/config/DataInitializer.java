//package com.fashion.leon.fashionshopbackend.config;
//
//import com.fashion.leon.fashionshopbackend.entity.Role;
//import com.fashion.leon.fashionshopbackend.repository.RoleRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer {
//    private final RoleRepository roleRepository;
//
//    @PostConstruct
//    public void ensureDefaultRolesExist() {
//        if (roleRepository.findByName("admin").isEmpty()) {
//            roleRepository.save(Role.builder().name("admin").description("Administrator role").build());
//        }
//        if (roleRepository.findByName("staff").isEmpty()) {
//            roleRepository.save(Role.builder().name("staff").description("Staff member role").build());
//        }
//        if (roleRepository.findByName("manager").isEmpty()) {
//            roleRepository.save(Role.builder().name("manager").description("Manager role").build());
//        }
//    }
//}
