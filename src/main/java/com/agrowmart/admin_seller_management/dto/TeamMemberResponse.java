package com.agrowmart.admin_seller_management.dto;

public record TeamMemberResponse

(Long id, String fullName, String email, String phone, String role, String photoUrl, boolean active) {}