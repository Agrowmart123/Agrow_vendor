package com.agrowmart.controller;

import com.agrowmart.dto.auth.OtpRequest;
import com.agrowmart.dto.auth.VerifyOtpRequest;
import com.agrowmart.dto.auth.doctor.DoctorProfileCreateDTO;
import com.agrowmart.dto.auth.doctor.DoctorProfileResponseDTO;
import com.agrowmart.entity.User;
import com.agrowmart.enums.OtpPurpose;
import com.agrowmart.service.AuthService;
import com.agrowmart.service.DoctorService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService; // Fixed: lowercase 'd'
    private final AuthService authService;
    public DoctorController(DoctorService doctorService,AuthService authService) {
        this.doctorService = doctorService;
        this.authService=authService;
    }
    // Create or UPDATE my profile (same endpoint for both)
    @PostMapping("/profile")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public DoctorProfileResponseDTO upsertProfile(
            @RequestBody DoctorProfileCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return doctorService.createOrUpdateProfile(user.getId(), dto);
    }

    // Get my own profile
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public DoctorProfileResponseDTO getMyProfile(@AuthenticationPrincipal User user) {
        return doctorService.getProfile(user.getId());
    }

    // Get public profile of any doctor
    @GetMapping("/{id}")
    public DoctorProfileResponseDTO getDoctor(@PathVariable Long id) {
        return doctorService.getPublicProfile(id);
    }
    
    
 // UPDATE Profile (Dedicated PUT endpoint - Best Practice)
    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public DoctorProfileResponseDTO updateProfile(
            @RequestBody DoctorProfileCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return doctorService.createOrUpdateProfile(user.getId(), dto);
    }
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest req) throws Exception {
        authService.sendOtp(req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + req.phone()
        ));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req);
        String msg = req.purpose() == OtpPurpose.FORGOT_PASSWORD
                ? "OTP verified! You can now reset password"
                : "Phone verified successfully!";
        return ResponseEntity.ok(Map.of("success", true, "message", msg));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Phone is required"));
        }
        try {
            authService.forgotPassword(phone);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset OTP sent to " + phone
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Get all doctors (public)
    @GetMapping("/all")
    public ResponseEntity<List<DoctorProfileResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // DELETE my profile
    @DeleteMapping("/profile")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> deleteMyProfile(@AuthenticationPrincipal User user) {
        doctorService.deleteProfile(user.getId());
        return ResponseEntity.ok().body("Profile deleted successfully");
    }
}