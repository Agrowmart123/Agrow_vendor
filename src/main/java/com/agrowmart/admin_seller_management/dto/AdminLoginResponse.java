package com.agrowmart.admin_seller_management.dto;

public record AdminLoginResponse(String token, Long id, String fullName, String email, String role, String photoUrl) {

}
