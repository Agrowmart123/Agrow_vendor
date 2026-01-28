package com.agrowmart.dto.auth;

import java.time.LocalTime;

public record MeResponse(
        Long id,
        String uuid,
        String name,
        String email,
        String phone,
        boolean phoneVerified,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String businessName,

        String udyamRegistrationNumber,
        String gstCertificateNumber,
        String tradeLicenseNumber,
        String fssaiLicenseNumber,

        String aadhaarImagePath,
        String panImagePath,
        String udyamRegistrationImagePath,
        String fssaiLicensePath,

        String bankName,
        String accountHolderName,
        String ifscCode,
        String upiId,

        String roleName,

        String photoUrl,
        String onlineStatus,

        boolean profileCompleted,
        String profileCompletedStr,
        int profileCompletionPercentage,

        String aadhaarStatus,
        String panStatus,
        String udhyamStatus,

        
        String shopName,
        String shopType,
        String shopAddress,
        String shopPhoto,
        String shopCoverPhoto,
        String shopLicensePhoto,
        String workingHours,
        String shopDescription,
        String shopLicense,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean isApproved,
        boolean isActive,
        
        
        String createdAt,
        String updatedAt
) {}
