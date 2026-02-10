package com.agrowmart.admin_seller_management.controller;

import com.agrowmart.admin_seller_management.entity.Admin;
import com.agrowmart.admin_seller_management.enums.AdminRole;
import com.agrowmart.admin_seller_management.repository.AdminRepository;
import com.agrowmart.exception.AuthExceptions.CloudinaryOperationException;
import com.agrowmart.exception.AuthExceptions.DuplicateResourceException;
import com.agrowmart.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/super/team")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperTeamController {

    private final AdminRepository adminRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public SuperTeamController(
            AdminRepository adminRepository,
            CloudinaryService cloudinaryService,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. GET - List all non-deleted team members
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTeamMembers() {
        List<Map<String, Object>> members = adminRepository.findByDeletedFalse()
                .stream()
                .map(admin -> {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", admin.getId());
                    member.put("fullName", admin.getFullName());
                    member.put("email", admin.getEmail());
                    member.put("phone", admin.getPhone());
                    member.put("role", admin.getRole().name());
                    member.put("photoUrl", admin.getPhotoUrl());
                    member.put("active", admin.isActive());
                    return member;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }

    // 2. POST - Add new team member (flat form-data + optional photo)
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> addTeamMember(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("role") String role,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        Map<String, Object> response = new HashMap<>();

        // 1. Check duplicate email
        if (adminRepository.existsByEmail(email.trim())) {
            throw new DuplicateResourceException("This email is already registered.");
        }

        // 2. Validate role
        Set<String> allowedRoles = Set.of("ADMIN", "SUB_ADMIN", "EDITOR");
        if (!allowedRoles.contains(role)) {
            throw new com.agrowmart.exception.AuthExceptions.BusinessValidationException(
                    "Invalid role. Allowed values: ADMIN, SUB_ADMIN, EDITOR");
        }

        // 3. Create new admin
        Admin newAdmin = new Admin();
        newAdmin.setFullName(fullName.trim());
        newAdmin.setEmail(email.trim());
        newAdmin.setPhone(phone.trim());
        newAdmin.setRole(AdminRole.valueOf(role));

        // Password handling
        String rawPassword = (password != null && !password.trim().isEmpty())
                ? password.trim()
                : UUID.randomUUID().toString().substring(0, 12);
        newAdmin.setPasswordHash(passwordEncoder.encode(rawPassword));

        newAdmin.setActive(true);

        // 4. Optional photo upload
        if (photo != null && !photo.isEmpty()) {
            try {
                String url = cloudinaryService.upload(photo);
                newAdmin.setPhotoUrl(url);
            } catch (Exception e) {
                throw new CloudinaryOperationException("Failed to upload photo to Cloudinary", e);
            }
        }

        // 5. Set creator
        Admin creator = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        newAdmin.setCreatedBy(creator);

        adminRepository.save(newAdmin);

        response.put("message", "Team member added successfully");
        response.put("id", newAdmin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 3. PUT - Update team member (flat form-data + optional photo)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> updateTeamMember(
            @PathVariable Long id,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        Map<String, Object> response = new HashMap<>();

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new com.agrowmart.exception.ResourceNotFoundException("Team member not found with id: " + id));

        if (admin.isSuperAdmin()) {
            throw new com.agrowmart.exception.AuthExceptions.BusinessValidationException("Cannot edit SUPER_ADMIN account");
        }

        // Update fields only if provided
        if (fullName != null && !fullName.trim().isEmpty()) {
            admin.setFullName(fullName.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            admin.setPhone(phone.trim());
        }
        if (role != null && !role.trim().isEmpty()) {
            String r = role.trim();
            Set<String> allowedRoles = Set.of("ADMIN", "SUB_ADMIN", "EDITOR");
            if (!allowedRoles.contains(r)) {
                throw new com.agrowmart.exception.AuthExceptions.BusinessValidationException(
                        "Invalid role. Allowed: ADMIN, SUB_ADMIN, EDITOR");
            }
            admin.setRole(AdminRole.valueOf(r));
        }

        // Optional photo update
        if (photo != null && !photo.isEmpty()) {
            try {
                // Delete old photo if exists
                if (admin.getPhotoUrl() != null && !admin.getPhotoUrl().isBlank()) {
                    cloudinaryService.delete(admin.getPhotoUrl());
                }
                String url = cloudinaryService.upload(photo);
                admin.setPhotoUrl(url);
            } catch (Exception e) {
                throw new CloudinaryOperationException("Failed to process photo (upload/delete) with Cloudinary", e);
            }
        }

        adminRepository.save(admin);

        response.put("message", "Team member updated successfully");
        return ResponseEntity.ok(response);
    }

    // 4. DELETE - Soft delete (unchanged)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteTeamMember(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) {
            response.put("error", "Not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (admin.isSuperAdmin()) {
            response.put("error", "Cannot delete SUPER_ADMIN");
            return ResponseEntity.badRequest().body(response);
        }

        Admin current = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (current.getId().equals(id)) {
            response.put("error", "Cannot delete yourself");
            return ResponseEntity.badRequest().body(response);
        }

        admin.markAsDeleted(current);
        adminRepository.save(admin);

        response.put("message", "Deactivated successfully");
        return ResponseEntity.ok(response);
    }
}