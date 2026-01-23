//
//package com.agrowmart.util;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.agrowmart.entity.User;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.expiration-ms}")
//    private long expirationMs;
//
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    /** Generates a JWT for the given User entity */
//    public String generateToken(User user) {
//        return Jwts.builder()
//        		.setSubject(String.valueOf(user.getId()))
//                .setSubject(user.getEmail())               // subject = email (fallback handled in JwtService)
//                .claim("userId", user.getId())
//                .claim("role", user.getRole().getName())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//    /** Parses and returns all claims */
//    public Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public String extractSubject(String token) {
//        return extractAllClaims(token).getSubject();
//    }
//
//    public Long extractUserId(String token) {
//        return extractAllClaims(token).get("userId", Long.class);
//    }
//
//    public String extractRole(String token) {
//        return extractAllClaims(token).get("role", String.class);
//    }
//
//    public boolean isTokenExpired(String token) {
//        return extractAllClaims(token).getExpiration().before(new Date());
//    }
//
//    /** Validates token against the provided email (subject) */
//    public boolean validateToken(String token, String email) {
//        return email.equals(extractSubject(token)) && !isTokenExpired(token);
//    }
//    public boolean validateToken(String token, Long userId) {
//        Long tokenUserId = extractUserId(token);
//        return tokenUserId != null 
//                && tokenUserId.equals(userId) 
//                && !isTokenExpired(token);
//    }
//
//    public String generateRefreshToken(Long userId) {
//        return Jwts.builder()
//            .setSubject(userId.toString())
//            .setIssuedAt(new Date())
//            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
//            .signWith(getSigningKey())
//            .compact();
//    }
//
//    public String generateToken(Long userId) {
//        return Jwts.builder()
//            .claim("userId", userId)
//            .setIssuedAt(new Date())
//            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//            .signWith(getSigningKey())
//            .compact();
//    }
//	
//}

// src/main/java/com/agrowmart/util/JwtUtil.java

package com.agrowmart.util;

import com.agrowmart.entity.User;
import com.agrowmart.entity.customer.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

   @Value("${jwt.expiration-ms:86400000}")
private long jwtExpirationMs;


   @Value("${jwt.secret:default-dev-secret-change-this}")
private String jwtSecret;


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==================== GENERATE TOKENS ====================

    public String generateTokenForVendor(User user) {
        return Jwts.builder()
                .claim("userId", user.getId())
                .claim("role", user.getRole().getName())
                .claim("type", "vendor")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenForCustomer(Customer customer) {
        return Jwts.builder()
                .claim("userId", customer.getId())
                .claim("role", "CUSTOMER")
                .claim("type", "customer")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ==================== EXTRACT CLAIMS ====================

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // THIS WAS MISSING — NOW ADDED!
    public String extractUserType(String token) {
        return extractAllClaims(token).get("type", String.class); // returns "vendor" or "customer"
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ==================== VALIDATION ====================

    public boolean validateToken(String token, Long userId) {
        Long tokenUserId = extractUserId(token);
        return tokenUserId != null && tokenUserId.equals(userId) && !isTokenExpired(token);
    }

    public boolean validateToken(String token, String email) {
        // Optional fallback if you ever store email as subject
        return !isTokenExpired(token);
    }
}