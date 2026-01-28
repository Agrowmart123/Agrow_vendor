//
//
//package com.agrowmart.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "users")
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(columnDefinition = "BIGINT UNSIGNED")
//    private Long id;
//
//    @Column(columnDefinition = "CHAR(36)", nullable = false)
//    private String uuid = UUID.randomUUID().toString();
//
//    @Column(length = 100, nullable = false)
//    private String name;
//
//    @Column(length = 255, unique = true, nullable = false)
//    private String email;
//
//    @Column(name = "password_hash", length = 255, nullable = false)
//    private String passwordHash;
//
//    @Column(length = 30, unique = true, nullable = false)
//    private String phone;
//
//    @Column(name = "phone_verified", columnDefinition = "TINYINT(1) DEFAULT 0")
//    private boolean phoneVerified = false;
//
//    @Column(columnDefinition = "TEXT")
//    private String address;
//
//    @Column(name = "kisan_card_number", length = 50)
//    private String kisanCardNumber;
//
//    @Column(name = "bank_account_number", length = 50)
//    private String bankAccountNumber;
//
//    @Column(name = "ifsc_code", length = 20)
//    private String ifscCode;
//
//    @Column(name = "id_proof_path", length = 255)
//    private String idProofPath;
//
//    @Column(length = 100)
//    private String city;
//
//    @Column(length = 100)
//    private String state;
//
//    @Column(length = 100)
//    private String country;
//
//    @Column(length = 255)
//    private String businessName;
//
//    @Column(name = "postal_code", length = 20)
//    private String postalCode;
//
//    // FIXED: These are now properly inside the class with @Column
//    @Column(name = "bank_name", length = 100)
//    private String bankName;
//
//    @Column(name = "account_holder_name", length = 100)
//    private String accountHolderName;
//
//    @Column(name = "photo_url", length = 500)
//    private String photo_url;
//
//    @Column(name = "profile_completed", length = 10)
//    private String profile_completed;  // or boolean if you prefer
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "role_id", nullable = false)
//    private Role role;
//
//    @CreationTimestamp
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // =============== Constructors ===============
//    public User() {}
//
//    public User(String name, String email, String passwordHash, String phone, Role role) {
//        this.name = name;
//        this.email = email;
//        this.passwordHash = passwordHash;
//        this.phone = phone;
//        this.role = role;
//    }
//
//    // =============== Getters & Setters ===============
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getUuid() { return uuid; }
//    public void setUuid(String uuid) { this.uuid = uuid; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getPasswordHash() { return passwordHash; }
//    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
//
//    public String getPhone() { return phone; }
//    public void setPhone(String phone) { this.phone = phone; }
//
//    public boolean isPhoneVerified() { return phoneVerified; }
//    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }
//
//    public String getAddress() { return address; }
//    public void setAddress(String address) { this.address = address; }
//
//    public String getKisanCardNumber() { return kisanCardNumber; }
//    public void setKisanCardNumber(String kisanCardNumber) { this.kisanCardNumber = kisanCardNumber; }
//
//    public String getBankAccountNumber() { return bankAccountNumber; }
//    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
//
//    public String getIfscCode() { return ifscCode; }
//    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
//
//    public String getIdProofPath() { return idProofPath; }
//    public void setIdProofPath(String idProofPath) { this.idProofPath = idProofPath; }
//
//    public String getCity() { return city; }
//    public void setCity(String city) { this.city = city; }
//
//    public String getState() { return state; }
//    public void setState(String state) { this.state = state; }
//
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//
//    public String getBusinessName() { return businessName; }
//    public void setBusinessName(String businessName) { this.businessName = businessName; }
//
//    public String getPostalCode() { return postalCode; }
//    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
//
//    public String getBankName() { return bankName; }
//    public void setBankName(String bankName) { this.bankName = bankName; }
//
//    public String getAccountHolderName() { return accountHolderName; }
//    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
//
//    @JsonProperty("photoUrl")  // This is the MAGIC line
//    public String getPhoto_url() { return photo_url; }
//    @JsonProperty("photoUrl")  // This is the MAGIC line
//    public void setPhoto_url(String photo_url) { this.photo_url = photo_url; }
//
//    public String getProfile_completed() { return profile_completed; }
//    public void setProfile_completed(String profile_completed) { this.profile_completed = profile_completed; }
//
//    public Role getRole() { return role; }
//    public void setRole(Role role) { this.role = role; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//    
//    
//    //============
// // User.java में ये दो लाइन होनी चाहिए
//    @Column(name = "fcm_token", length = 1000)
//    private String fcmToken;
//
//    // Getter & Setter
//    public String getFcmToken() { return fcmToken; }
//    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
//}


//-------------------------------------------




package com.agrowmart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.agrowmart.admin_seller_management.enums.AccountStatus;
import com.agrowmart.admin_seller_management.enums.DocumentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 15, unique = true)
    private String phone;

    @Column(name = "phone_verified", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean phoneVerified = false;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country = "India";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 255)
    private String businessName;

    // ==================== KYC & Document Fields ====================

    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "udyam_registration_number", length = 20)  // Increased length for safety (Udyam is UDYAM-XX-00-0000000)
    private String udyamRegistrationNumber;
    
    // Optional GST
    @Column(name = "gst_certificate_number", length = 15)
    private String gstCertificateNumber;


    // Trade License - Only number (as requested)
    @Column(name = "trade_license_number", length = 100)
    private String tradeLicenseNumber;
    
    //Optional
       @Column(name = "fssai_license_number", length = 50)
    private String fssaiLicenseNumber;

    @Column(name = "fssai_license_path", length = 500)
    private String fssaiLicensePath;

    // Health & Safety Certificate - Only number Optional
    @Column(name = "health_safety_certificate_number", length = 100)
    private String healthSafetyCertificateNumber;

    @Column(name = "aadhaar_image_path", length = 500)
    private String aadhaarImagePath;

    @Column(name = "pan_image_path", length = 500)
    private String panImagePath;

    @Column(name = "udyam_registration_image_path", length = 500)
    private String udyamRegistrationImagePath;
    
    
    
    

  


	public String getUdyamRegistrationNumber() {
		return udyamRegistrationNumber;
	}

	public void setUdyamRegistrationNumber(String udyamRegistrationNumber) {
		this.udyamRegistrationNumber = udyamRegistrationNumber;
	}

	public String getAadhaarImagePath() {
		return aadhaarImagePath;
	}

	public void setAadhaarImagePath(String aadhaarImagePath) {
		this.aadhaarImagePath = aadhaarImagePath;
	}

	public String getPanImagePath() {
		return panImagePath;
	}

	public void setPanImagePath(String panImagePath) {
		this.panImagePath = panImagePath;
	}

	public String getUdyamRegistrationImagePath() {
		return udyamRegistrationImagePath;
	}

	public void setUdyamRegistrationImagePath(String udyamRegistrationImagePath) {
		this.udyamRegistrationImagePath = udyamRegistrationImagePath;
	}

	// ==================== Bank Details ====================
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_holder_name", length = 100)
    private String accountHolderName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    // ==================== Profile ====================
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "profile_completed", columnDefinition = "ENUM('YES','NO') DEFAULT 'NO'")
    private String profileCompleted = "NO";

    @Column(name = "fcm_token", length = 1000)
    private String fcmToken;

    // ==================== Relations & Timestamps ====================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== Constructors ====================
    public User() {}

    public User(String name, String email, String passwordHash, String phone, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    // KYC Getters & Setters
    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getGstCertificateNumber() { return gstCertificateNumber; }
    public void setGstCertificateNumber(String gstCertificateNumber) { this.gstCertificateNumber = gstCertificateNumber; }



    public String getTradeLicenseNumber() { return tradeLicenseNumber; }
    public void setTradeLicenseNumber(String tradeLicenseNumber) { this.tradeLicenseNumber = tradeLicenseNumber; }

    public String getFssaiLicenseNumber() { return fssaiLicenseNumber; }
    public void setFssaiLicenseNumber(String fssaiLicenseNumber) { this.fssaiLicenseNumber = fssaiLicenseNumber; }

    public String getFssaiLicensePath() { return fssaiLicensePath; }
    public void setFssaiLicensePath(String fssaiLicensePath) { this.fssaiLicensePath = fssaiLicensePath; }

    public String getHealthSafetyCertificateNumber() { return healthSafetyCertificateNumber; }
    public void setHealthSafetyCertificateNumber(String healthSafetyCertificateNumber) {
        this.healthSafetyCertificateNumber = healthSafetyCertificateNumber;
    }


    // Bank Details
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    @JsonProperty("photoUrl")
    public String getPhotoUrl() { return photoUrl; }

    @JsonProperty("photoUrl")
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(String profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
   //----------------
 // NEW: Online/Offline Status for Vendors
    @Column(name = "online_status", columnDefinition = "ENUM('ONLINE','OFFLINE') DEFAULT 'OFFLINE'")
    private String onlineStatus = "OFFLINE";

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
//---------------------
	
	// Add this field
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Subscription> subscriptions = new ArrayList<>();

//	// Helper method (very useful)
//	public Subscription getActiveSubscription() {
//	    return subscriptions.stream()
//	            .filter(Subscription::isActive)
//	            .filter(s -> s.getExpiryDate().isAfter(LocalDateTime.now()))
//	            .findFirst()
//	            .orElse(null);
//	}
	public Subscription getActiveSubscription() {
	    return subscriptions.stream()
	            .filter(Subscription::isActive)
	            .filter(s -> s.getExpiryDate().isAfter(LocalDateTime.now()))
	            .findFirst()
	            .orElse(null);
	}
	public boolean hasActiveSubscription() {
	    Subscription sub = getActiveSubscription();
	    return sub != null;
	}

	public int getCurrentProductLimit() {
	    Subscription sub = getActiveSubscription();
	    return sub != null ? sub.getPlan().getMaxProducts() : 0;
	}

	
	//Added by Aakanksha - 13/01/2026
		@Column(name = "account_status",
		        columnDefinition = "ENUM('PENDING','APPROVED','REJECTED','BLOCKED') DEFAULT 'PENDING'")
		@Enumerated(EnumType.STRING)
		private AccountStatus accountStatus;

		public AccountStatus getAccountStatus() {
		    return accountStatus;
		}

		public void setAccountStatus(AccountStatus accountStatus) {
		    this.accountStatus = accountStatus;
		}
		//Added by Aakanksha - 19/01/2026
		
		@Column(name = "status_reason", length = 500)
		private String statusReason;

		@Column(name = "status_updated_at")
		private LocalDateTime statusUpdatedAt;

		public String getStatusReason() {
		    return statusReason;
		}

		public void setStatusReason(String statusReason) {
		    this.statusReason = statusReason;
		}

		public LocalDateTime getStatusUpdatedAt() {
		    return statusUpdatedAt;
		}

		public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) {
		    this.statusUpdatedAt = statusUpdatedAt;
		}
		
		@Enumerated(EnumType.STRING)
		private DocumentStatus aadhaarStatus = DocumentStatus.PENDING;

		@Enumerated(EnumType.STRING)
		private DocumentStatus panStatus = DocumentStatus.PENDING;
		

	@Enumerated(EnumType.STRING)
	private DocumentStatus udhyamStatus = DocumentStatus.PENDING;

	@Column(length = 500)
	private String rejectionReason;




	public DocumentStatus getAadhaarStatus() {
		return aadhaarStatus;
	}

	public void setAadhaarStatus(DocumentStatus aadhaarStatus) {
		this.aadhaarStatus = aadhaarStatus;
	}

	public DocumentStatus getPanStatus() {
		return panStatus;
	}

	public void setPanStatus(DocumentStatus panStatus) {
		this.panStatus = panStatus;
	}

	public DocumentStatus getUdhyamStatus() {
		return udhyamStatus;
	}

	public void setUdhyamStatus(DocumentStatus udhyamStatus) {
		this.udhyamStatus = udhyamStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
	
	// Add this field in User entity
	// In User.java – keep this as is
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Shop shop;






	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}



}