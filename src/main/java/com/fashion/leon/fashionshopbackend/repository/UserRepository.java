package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndIsActiveTrueFetchRoles(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndIsActiveTrueFetchRolesAndPermissions(@Param("email") String email);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r " +
            "WHERE (:q IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "  AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "  AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN u.roles r " +
                   "WHERE (:q IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
                   "   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))) " +
                   "  AND (:isActive IS NULL OR u.isActive = :isActive) " +
                   "  AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))")
    Page<User> searchUsers(@Param("q") String q,
                           @Param("isActive") Boolean isActive,
                           @Param("role") String role,
                           Pageable pageable);
}