package com.agrowmart.entity;



import com.agrowmart.admin_seller_management.enums.DocumentStatus;
import com.agrowmart.entity.User;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "shops", uniqueConstraints = {
 @UniqueConstraint(columnNames = "user_id")
})

public class Shop {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 

 @Column(nullable = false, length = 150)
 private String shopName;

 @Column(nullable = false, length = 50)
 private String shopType;

 @Column(nullable = false, length = 300)
 private String shopAddress;

 @Column(length = 500)
 private String shopPhoto;

 @Column(length = 500)
 private String shopCoverPhoto;

 @Column(length = 500)
 private String shopLicensePhoto;           // URL as String

 @Column(length = 1000)
 private String workingHours;

 @Column(length = 1000)
 private String shopDescription;

 @Column(nullable = false, length = 100)
 private String shopLicense;

 private LocalTime opensAt;
 private LocalTime closesAt;

 @Column(name = "is_approved", nullable = false)
 private boolean isApproved = false;

 @Column(name = "is_active", nullable = false)
 private boolean isActive = true;

 @OneToOne
 @JoinColumn(name = "user_id", nullable = false, unique = true)
 private User user;
 
	public Long getId() { 
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopType() {
		return shopType;
	}

	public void setShopType(String shopType) {
		this.shopType = shopType;
	}

	public String getShopAddress() {
		return shopAddress;
	}

	public void setShopAddress(String shopAddress) {
		this.shopAddress = shopAddress;
	}

	public String getShopPhoto() {
		return shopPhoto;
	}

	public void setShopPhoto(String shopPhoto) {
		this.shopPhoto = shopPhoto;
	}

	public String getShopCoverPhoto() {
		return shopCoverPhoto;
	}

	public void setShopCoverPhoto(String shopCoverPhoto) {
		this.shopCoverPhoto = shopCoverPhoto;
	}

	public String getShopLicensePhoto() {
		return shopLicensePhoto;
	}

	public void setShopLicensePhoto(String shopLicensePhoto) {
		this.shopLicensePhoto = shopLicensePhoto;
	}

	public String getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(String workingHours) {
		this.workingHours = workingHours;
	}

	public String getShopDescription() {
		return shopDescription;
	}

	public void setShopDescription(String shopDescription) {
		this.shopDescription = shopDescription;
	}

	public String getShopLicense() {
		return shopLicense;
	}

	public void setShopLicense(String shopLicense) {
		this.shopLicense = shopLicense;
	}

	public LocalTime getOpensAt() {
		return opensAt;
	}

	public void setOpensAt(LocalTime opensAt) {
		this.opensAt = opensAt;
	}

	public LocalTime getClosesAt() {
		return closesAt;
	}

	public void setClosesAt(LocalTime closesAt) {
		this.closesAt = closesAt;
	}

	public boolean isApproved() {
		return isApproved;
	}

	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	//Added By Aakanksha - 19/01/2026
		@Enumerated(EnumType.STRING)
		private DocumentStatus shopLicensePhotoStatus = DocumentStatus.PENDING;

		public DocumentStatus getShopLicensePhotoStatus() {
		    return shopLicensePhotoStatus;
		}

		public void setShopLicensePhotoStatus(DocumentStatus shopLicensePhotoStatus) {
		    this.shopLicensePhotoStatus = shopLicensePhotoStatus;
		}

	
 
}