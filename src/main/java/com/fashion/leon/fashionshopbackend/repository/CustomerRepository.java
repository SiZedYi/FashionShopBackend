package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Customer> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses WHERE c.email = :email AND c.isActive = true")
    Optional<Customer> findByEmailAndIsActiveTrueFetchAddresses(@Param("email") String email);
}

