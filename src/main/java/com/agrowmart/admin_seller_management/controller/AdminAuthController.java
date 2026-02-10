package com.agrowmart.admin_seller_management.controller;

import com.agrowmart.admin_seller_management.dto.AdminLoginResponse;
import com.agrowmart.admin_seller_management.dto.LoginRequest;
import com.agrowmart.admin_seller_management.entity.Admin;
import com.agrowmart.admin_seller_management.repository.AdminRepository;
import com.agrowmart.admin_seller_management.service.EmailService;
import com.agrowmart.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AdminAuthController(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmailService emailService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // 1. Login - Returns only safe fields + token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!admin.isActive() || admin.isDeleted()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Account inactive"));
        }

        String token = jwtUtil.generateTokenForAdmin(admin);

        // Return minimal data (no sensitive fields)
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", admin.getId());
        response.put("fullName", admin.getFullName());
        response.put("email", admin.getEmail());
        response.put("role", admin.getRole().name());
        response.put("photoUrl", admin.getPhotoUrl());

        return ResponseEntity.ok(response);
    }

    // 2. Forgot Password - Send reset link via email (minimal response)
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        Admin admin = adminRepository.findByEmail(email.trim()).orElse(null);

        // Always return same message for security (don't reveal if email exists)
        if (admin == null || !admin.isActive() || admin.isDeleted()) {
            return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
        }

        // Generate secure reset token
        String resetToken = UUID.randomUUID().toString();

        // TODO: Save token + expiry to DB (add fields to Admin entity: resetToken, resetTokenExpiry)
        // Example (uncomment when you add fields):
        // admin.setResetToken(resetToken);
        // admin.setResetTokenExpiry(LocalDateTime.now().plusHours(2));
        // adminRepository.save(admin);

        String resetLink = "https://agrowmart.vercel.app/admin/reset-password?token=" + resetToken + "&email=" + email;

        // Send email
        emailService.sendSimpleEmail(
                email,
                "AgrowMart Admin - Password Reset Request",
                "Hello,\n\nYou requested a password reset.\nClick this link to reset your password:\n"
                        + resetLink + "\n\nThis link is valid for 2 hours.\n\nIf you did not request this, ignore this email."
        );

        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
    }
}

