package com.agrowmart.controller;

import com.agrowmart.dto.auth.JwtResponse;
import com.agrowmart.dto.auth.customer.*;
import com.agrowmart.entity.customer.Customer;
import com.agrowmart.enums.OtpPurpose;
import com.agrowmart.exception.*;
import com.agrowmart.service.customer.CustomerAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.TooManyListenersException;

@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private static final Logger log = LoggerFactory.getLogger(CustomerAuthController.class);

    private final CustomerAuthService customerAuthService;

    public CustomerAuthController(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    // ──────────────────────────────────────────────
    // 1. Register (auto-sends OTP)
    // ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Customer> register(@Valid @RequestBody CustomerRegisterRequest req) throws Exception {
        Customer customer = customerAuthService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    // ──────────────────────────────────────────────
    // 2. Login
    // ──────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody CustomerLoginRequest req) {
        JwtResponse response = customerAuthService.login(req);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────
    // 3. Get My Profile (Authenticated)
    // ──────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<CustomerProfileResponse> getProfile(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(customerAuthService.getProfile(customer));
    }

    // ──────────────────────────────────────────────
    // 4. Upload Profile Photo (Authenticated)
    // ──────────────────────────────────────────────
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPhoto(
            @RequestParam("photo") MultipartFile file,
            @AuthenticationPrincipal Customer customer) throws IOException {
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String photoUrl = customerAuthService.uploadPhoto(file, customer);
        return ResponseEntity.ok(photoUrl);
    }

    // ──────────────────────────────────────────────
    // 5. Send OTP (with rate limiting already in service)
    // ──────────────────────────────────────────────
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequestDto req) {
        try {
            customerAuthService.sendOtp(req.phone(), OtpPurpose.valueOf(req.purpose()));
            return ResponseEntity.ok("OTP sent successfully to your phone");
        } catch (TooManyListenersException e) {
            return (ResponseEntity<String>) ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e) {
            log.error("OTP send failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP");
        }
    }

    // ──────────────────────────────────────────────
    // 6. Verify OTP
    // ──────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto req) {
        try {
            customerAuthService.verifyOtp(req);
            return ResponseEntity.ok("OTP verified successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 7. Forgot Password (sends reset OTP)
    // ──────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto req) throws Exception {
        try {
            customerAuthService.forgotPassword(req.phone());
            return ResponseEntity.ok("Password reset OTP sent to your phone");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 8. Reset Password (after OTP verify)
    // ──────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto req) {
        try {
            customerAuthService.resetPassword(req.phone(), req.newPassword(), req.code());
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 9. Update Profile (PATCH - partial updates + photo)
    // ──────────────────────────────────────────────
    @PatchMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @AuthenticationPrincipal Customer customer,
            @RequestPart(value = "fullName", required = false) String fullName,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "gender", required = false) String gender,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImageFile,
            @RequestPart(value = "phone", required = false) String newPhone) throws IOException {

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Phone change is blocked here for security (needs separate OTP flow)
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null); // or throw exception
        }

        Customer updated = customerAuthService.updateProfile(
                customer, fullName, email, gender, profileImageFile, newPhone);

        CustomerProfileResponse response = new CustomerProfileResponse(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getPhone(),
                updated.getGender(),
                updated.getProfileImage(),
                updated.isPhoneVerified()
        );

        return ResponseEntity.ok(response);
    }
}