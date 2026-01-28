// src/main/java/com/agrowmart/service/customer/CustomerAuthService.java

package com.agrowmart.service.customer;

import com.agrowmart.dto.auth.JwtResponse;
import com.agrowmart.dto.auth.customer.*;
import com.agrowmart.entity.customer.Customer;
import com.agrowmart.enums.OtpPurpose;
import com.agrowmart.repository.customer.CustomerRepository;
import com.agrowmart.service.CloudinaryService;
import com.agrowmart.service.Fast2SmsService;
import com.agrowmart.service.JwtService;
import com.agrowmart.util.InMemoryOtpStore;
import com.agrowmart.util.InMemoryOtpStore.OtpData;
import com.agrowmart.util.RateLimiterUtil;
import com.agrowmart.util.RedisOtpStore;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomerAuthService {

	
	
	
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;
    private final Fast2SmsService fast2SmsService;  // NEW
    private final RedisOtpStore redisOtpStore;

    public CustomerAuthService(CustomerRepository customerRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               CloudinaryService cloudinaryService,
                               Fast2SmsService fast2SmsService,
                               RedisOtpStore redisOtpStore) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.cloudinaryService = cloudinaryService;
        this.fast2SmsService = fast2SmsService;
        this.redisOtpStore = redisOtpStore;
    }


    @Transactional
    public Customer register(CustomerRegisterRequest req) throws Exception {
        String phone = normalizePhone(req.phone());
        String email = req.email() != null ? req.email().trim().toLowerCase() : null;

        if (email != null && customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (customerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        Customer customer = new Customer();
        customer.setFullName(req.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(req.password()));
        customer.setPhoneVerified(false);
        customer.setActive(true);

        Customer saved = customerRepository.save(customer);

        // Auto-send OTP for phone verification
//        sendOtp(phone, OtpPurpose.PHONE_VERIFY);

        return saved;
    }
    
    

    public JwtResponse login(CustomerLoginRequest req) {
        String input = req.login().trim();
        Optional<Customer> customerOpt = customerRepository.findByEmail(input);

        if (customerOpt.isEmpty()) {
            String phone = normalizePhone(input);
            customerOpt = customerRepository.findByPhone(phone);
        }

        Customer customer = customerOpt.orElseThrow(
                () -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.issueTokenForCustomer(customer);
        return new JwtResponse(token, null, LocalDateTime.now().plusDays(7));
    }

    public CustomerProfileResponse getProfile(Customer customer) {
        return new CustomerProfileResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getGender(),
                customer.getProfileImage(),
                customer.isPhoneVerified()
        );
    }

    @Transactional
    public String uploadPhoto(MultipartFile file, Customer customer) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String photoUrl = cloudinaryService.upload(file);
        customer.setProfileImage(photoUrl);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
        return photoUrl;
    }

    
  //--------------------  
 // UPDATED: Now using Fast2SMS instead of Twilio
//    @Transactional
//    public void sendOtp(String phone, OtpPurpose purpose) {
//        String normalizedPhone = normalizePhone(phone);
//        
//     // Rate limiting
//        String rateKey = "rate:otp:customer:" + normalizedPhone;
//        if (!RateLimiterUtil.isAllowed(rateKey, MAX_OTP_PER_HOUR, RATE_LIMIT_WINDOW_SECONDS)) {
//            log.warn("OTP rate limit exceeded for customer phone: {}", normalizedPhone);
//            throw new Exception("Too many OTP requests. Try again after 1 hour.");
//        }
//        String code = String.format("%06d", new SecureRandom().nextInt(999999));
//
//        InMemoryOtpStore.saveOtp(normalizedPhone, code, purpose.name(), 300);
//
//        String message = "AgroMart OTP: " + code + " (valid 5 min)";
//
//        // Send via Fast2SMS (Twilio removed)
//        fast2SmsService.sendOtp(normalizedPhone, code, purpose.name());
//    }
//    
    
//    
    
    @Transactional
    public void sendOtp(String phone, OtpPurpose purpose) throws Exception {
        redisOtpStore.sendOtp(phone, purpose);
    }
    

    @Transactional
    public void verifyOtp(VerifyOtpRequestDto req) {
        String normalizedPhone = normalizePhone(req.phone());
        OtpPurpose purpose = OtpPurpose.valueOf(req.purpose());

        boolean valid = redisOtpStore.verifyOtp(normalizedPhone, req.code(), purpose);

        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        
        if (purpose == OtpPurpose.PHONE_VERIFY) {
            Customer customer = customerRepository.findByPhone(normalizedPhone)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            customer.setPhoneVerified(true);
            customerRepository.save(customer);
        }
    }

    @Transactional
    public void forgotPassword(String phone) throws Exception {
        String normalized = normalizePhone(phone);
        if (!customerRepository.existsByPhone(normalized)) {
            throw new IllegalArgumentException("No account found with this phone number");
        }
        sendOtp(normalized, OtpPurpose.FORGOT_PASSWORD);
    }

    @Transactional
    public void resetPassword(String phone, String newPassword, String code) {
        String normalized = normalizePhone(phone);

        VerifyOtpRequestDto verifyReq = new VerifyOtpRequestDto(normalized, code, OtpPurpose.FORGOT_PASSWORD.name());
        verifyOtp(verifyReq);

        Customer customer = customerRepository.findByPhone(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
        customerRepository.save(customer);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Phone cannot be empty");
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("91") && cleaned.length() > 10) cleaned = cleaned.substring(cleaned.length() - 10);
        else if (cleaned.startsWith("0") && cleaned.length() > 10) cleaned = cleaned.substring(1);
        if (!cleaned.matches("^[6-9]\\d{9}$")) throw new IllegalArgumentException("Invalid mobile number");
        return "+91" + cleaned;
    }
    
    
    
    
  //----------------------------------------
    
    
    @Transactional
    public Customer updateProfile(
            Customer customer,
            String fullName,
            String email,
            String gender,
            MultipartFile profileImageFile,
            String newPhone
    ) throws IOException {

        boolean changed = false;

        // 1. Full Name
        if (fullName != null && !fullName.trim().isEmpty()) {
            customer.setFullName(fullName.trim());
            changed = true;
        }

        // 2. Email (optional - you can add uniqueness check)
        if (email != null && !email.trim().isEmpty()) {
            String normalizedEmail = email.trim().toLowerCase();
            if (!normalizedEmail.equals(customer.getEmail())) {
                if (customerRepository.existsByEmail(normalizedEmail)) {
                    throw new IllegalArgumentException("Email already in use");
                }
                customer.setEmail(normalizedEmail);
                changed = true;
            }
        }

        // 3. Gender (validate allowed values if you want)
        if (gender != null && !gender.trim().isEmpty()) {
            String g = gender.trim().toUpperCase();
            if (!Set.of("MALE", "FEMALE", "OTHER").contains(g)) {
                throw new IllegalArgumentException("Invalid gender value");
            }
            customer.setGender(g);
            changed = true;
        }

        // 4. Profile Image upload
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            if (!profileImageFile.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed for profile photo");
            }
            String photoUrl = cloudinaryService.upload(profileImageFile);
            customer.setProfileImage(photoUrl);
            changed = true;
        }

        // 5. Phone change (very sensitive - usually needs OTP verification)
        // For security, we recommend NOT allowing direct phone change here
        // If you really want to support it, implement OTP flow separately
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number change is not allowed via this endpoint. " +
                    "Please use a separate OTP-verified flow.");
        }

        if (!changed) {
            throw new IllegalArgumentException("No fields were provided to update");
        }

        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }
}