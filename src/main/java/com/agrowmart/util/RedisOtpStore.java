package com.agrowmart.util;

import com.agrowmart.enums.OtpPurpose;
import com.agrowmart.service.Fast2SmsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Component
public class RedisOtpStore {

    private static final Logger log = LoggerFactory.getLogger(RedisOtpStore.class);

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final int OTP_EXPIRY_SECONDS = 300;           // 5 minutes
    private static final int MAX_OTP_PER_HOUR = 5;               // Rate limit: 5 OTPs/hour/phone
    private static final int RATE_LIMIT_WINDOW_SECONDS = 3600;   // 1 hour

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private Fast2SmsService fast2SmsService;   // Your SMS sender

    @Autowired
    private RateLimiterUtil rateLimiterUtil;   // Your rate limiter

    /**
     * Send OTP with rate limiting
     * @throws Exception 
     */
    public void sendOtp(String phone, OtpPurpose purpose) throws Exception {
        String normalizedPhone = normalizePhone(phone);

        // Rate limiting: max 5 OTPs per hour per phone
        String rateKey = "rate:otp:" + normalizedPhone;
        if (!rateLimiterUtil.isAllowed(rateKey, MAX_OTP_PER_HOUR, RATE_LIMIT_WINDOW_SECONDS)) {
            log.warn("Rate limit exceeded for phone: {}", normalizedPhone);
            throw new Exception("Too many OTP requests. Please try again after 1 hour.");
        }

        // Generate 6-digit OTP
        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        // Store in Redis with TTL
        String otpKey = OTP_KEY_PREFIX + normalizedPhone;
        redisTemplate.opsForValue().set(otpKey, code, OTP_EXPIRY_SECONDS, TimeUnit.SECONDS);

        log.info("OTP generated and stored for phone: {}, purpose: {}, code: {}", 
                 normalizedPhone, purpose, code);

        // Send via Fast2SMS (async recommended in real prod)
        try {
            fast2SmsService.sendOtp(normalizedPhone, code, purpose.name());
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", normalizedPhone, e.getMessage(), e);
            // Optional: still return success to user, but log/alert
        }
    }

    /**
     * Verify OTP
     * @return true if valid, false otherwise
     */
    public boolean verifyOtp(String phone, String code, OtpPurpose purpose) {
        String normalizedPhone = normalizePhone(phone);
        String otpKey = OTP_KEY_PREFIX + normalizedPhone;

        String storedCode = redisTemplate.opsForValue().get(otpKey);

        if (storedCode == null) {
            log.warn("OTP not found or expired for phone: {}", normalizedPhone);
            return false;
        }

        if (!storedCode.equals(code.trim())) {
            log.warn("Invalid OTP attempt for phone: {}", normalizedPhone);
            return false;
        }

        // OTP is valid → delete it immediately (single use)
        redisTemplate.delete(otpKey);
        log.info("OTP verified successfully for phone: {}", normalizedPhone);

        return true;
    }

    /**
     * Indian phone normalization (very important for consistency)
     */
    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("91") && cleaned.length() > 10) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("0") && cleaned.length() > 10) {
            cleaned = cleaned.substring(1);
        }
        if (!cleaned.matches("^[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Invalid Indian mobile number format");
        }
        return cleaned;   // ← 10 digits only
    }
}