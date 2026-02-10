package com.agrowmart.admin_seller_management.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.agrowmart.admin_seller_management.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Admin> findByDeletedFalse();
}