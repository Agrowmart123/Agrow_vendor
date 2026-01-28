package com.agrowmart.controller;

import com.agrowmart.dto.auth.*;
import com.agrowmart.dto.auth.farmer.FarmerProfileRequest;
import com.agrowmart.dto.auth.farmer.FarmerRegisterRequest;
import com.agrowmart.entity.FarmerProfile;
import com.agrowmart.entity.User;
import com.agrowmart.enums.OtpPurpose;
import com.agrowmart.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    private final AuthService authService;

    public FarmerController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody FarmerRegisterRequest req) {
        return ResponseEntity.ok(authService.registerFarmer(req));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequest req) throws Exception {
        authService.sendOtp(req);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-password")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req);
        String msg = req.purpose() == OtpPurpose.FORGOT_PASSWORD
                ? "Password reset successful"
                : "Phone verified successfully";
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        JwtResponse jwt = authService.login(req);
        User user = authService.getUserFromToken(jwt.token());
        if (user == null || !"FARMER".equals(user.getRole().getName())) {
            return ResponseEntity.status(403).body(null);
        }
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) throws Exception {
        authService.forgotPassword(req.phone());
        return ResponseEntity.ok("OTP sent to reset password");
    }

    @PutMapping("/profile")
    public ResponseEntity<User> completeProfile(
            @Valid @RequestBody FarmerProfileRequest req,
            @AuthenticationPrincipal User farmer) {
        if (farmer == null || !"FARMER".equals(farmer.getRole().getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.updateFarmerFullProfile(req, farmer));
    }

    @PatchMapping("/update-data")
    public ResponseEntity<String> updateData(
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal User farmer) {
        if (farmer == null || !"FARMER".equals(farmer.getRole().getName())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        authService.updateFarmerPartialData(farmer, updates);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping(value = "/upload-photo", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPhoto(
    		@RequestParam("photo") MultipartFile photo ,  // ← Frontend must send key = "photo"
            @AuthenticationPrincipal User farmer) {
        if (farmer == null || !"FARMER".equals(farmer.getRole().getName())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(authService.uploadProfilePhoto(photo, farmer));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(@AuthenticationPrincipal User farmer) {
        if (farmer == null || !"FARMER".equals(farmer.getRole().getName())) {
            return ResponseEntity.status(403).build();
        }
        FarmerProfile profile = authService.getFarmerProfile(farmer);
        return ResponseEntity.ok(Map.of(
            "user", farmer,
            "farmerProfile", profile != null ? profile : Map.of()
        ));
    }
}