package com.agrowmart.admin_seller_management.dto;

public record AddTeamMemberRequest
(String fullName, String email, String phone, String role, String password) {}