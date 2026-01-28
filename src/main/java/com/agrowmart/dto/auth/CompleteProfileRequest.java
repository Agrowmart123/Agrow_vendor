package com.agrowmart.dto.auth;

import org.springframework.web.bind.annotation.RequestPart;

//src/main/java/com/agrowmart/dto/auth/CompleteProfileRequest.java

import org.springframework.web.multipart.MultipartFile;

public record CompleteProfileRequest(
 String businessName,
 String address,
 String city,
 String state,
 String country,
 String postalCode,

 String aadhaarNumber,
 String panNumber,
 String udyamRegistrationNumber,
 
 String gstCertificateNumber,
 String tradeLicenseNumber,
 String fssaiLicenseNumber,

 String bankName,
 String accountHolderName,
 String bankAccountNumber,
 String ifscCode,
 String upiId,

 MultipartFile fssaiLicenseFile,
 MultipartFile photo,
 
//NEW: Specific document uploads
 MultipartFile aadhaarImage,
 MultipartFile panImage,
 MultipartFile udyamRegistrationImage,
 
 
 
//── NEW: Optional Shop creation/updation fields ────────────
 @RequestPart(required = false) String shopName,
 @RequestPart(required = false) String shopType,
 @RequestPart(required = false) String shopAddress,
 @RequestPart(required = false) String workingHours,
 @RequestPart(required = false) String shopDescription,
 @RequestPart(required = false) String shopLicense,
 @RequestPart(required = false) MultipartFile shopPhoto,
 @RequestPart(required = false) MultipartFile shopCoverPhoto,
 @RequestPart(required = false) MultipartFile shopLicensePhoto,
 @RequestPart(required = false) String opensAt,   // "HH:mm"
 @RequestPart(required = false) String closesAt   // "HH:mm"
 
) {}