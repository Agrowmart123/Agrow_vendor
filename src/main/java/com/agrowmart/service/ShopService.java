package com.agrowmart.service;



import com.agrowmart.dto.auth.shop.ShopRequest;
import com.agrowmart.dto.auth.shop.ShopResponse;
import com.agrowmart.entity.Shop;
import com.agrowmart.entity.User;
import com.agrowmart.repository.ShopRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service

public class ShopService {

 private final ShopRepository shopRepository;
 private final CloudinaryService cloudinaryService; // ✅ ONLY cloudinary
 
// ✅ MANUAL CONSTRUCTOR (REQUIRED)
 public ShopService(ShopRepository shopRepository,
                    CloudinaryService cloudinaryService) {
     this.shopRepository = shopRepository;
     this.cloudinaryService = cloudinaryService;
 }

 
 private static final Set<String> VENDOR_ROLES = Set.of(
         "FARMER", "VEGETABLE", "DAIRY", "SEAFOODMEAT", "WOMEN"
 );

 // ===================== CREATE SHOP =====================
 public Shop createShop(ShopRequest req, User user) throws IOException {

     if (!isVendor(user)) {
         throw new RuntimeException("Only vendors can create a shop");
     }

     if (shopRepository.existsByUser(user)) {
         throw new RuntimeException("You already have a shop");
     }

     Shop shop = new Shop();
     shop.setShopName(req.shopName());
     shop.setShopType(req.shopType());
     shop.setShopAddress(req.shopAddress());
     shop.setWorkingHours(req.workingHours());
     shop.setShopDescription(req.shopDescription());
     shop.setShopLicense(req.shopLicense());
     shop.setUser(user);
     shop.setApproved(false);
     shop.setActive(true);

     shop.setOpensAt(req.opensAt());
     shop.setClosesAt(req.closesAt());

     // ✅ CLOUDINARY UPLOAD
     shop.setShopPhoto(uploadIfPresent(req.shopPhoto()));
     shop.setShopCoverPhoto(uploadIfPresent(req.shopCoverPhoto()));
     shop.setShopLicensePhoto(uploadIfPresent(req.shopLicensePhoto()));

     return shopRepository.save(shop);
 }

 // ===================== UPDATE MY SHOP =====================
 public Shop updateMyShop(ShopRequest req, User user) throws IOException {

     if (!isVendor(user)) {
         throw new RuntimeException("Only vendors can update shop");
     }

     Shop shop = shopRepository.findByUser(user)
             .orElseThrow(() -> new RuntimeException("Shop not found"));

     shop.setShopName(req.shopName());
     shop.setShopType(req.shopType());
     shop.setShopAddress(req.shopAddress());
     shop.setWorkingHours(req.workingHours());
     shop.setShopDescription(req.shopDescription());
     shop.setShopLicense(req.shopLicense());

     shop.setOpensAt(req.opensAt());
     shop.setClosesAt(req.closesAt());

     // ✅ Replace image only if new one is provided
     if (req.shopPhoto() != null && !req.shopPhoto().isEmpty()) {
         if (shop.getShopPhoto() != null) {
             cloudinaryService.delete(shop.getShopPhoto());
         }
         shop.setShopPhoto(uploadIfPresent(req.shopPhoto()));
     }

     if (req.shopCoverPhoto() != null && !req.shopCoverPhoto().isEmpty()) {
         if (shop.getShopCoverPhoto() != null) {
             cloudinaryService.delete(shop.getShopCoverPhoto());
         }
         shop.setShopCoverPhoto(uploadIfPresent(req.shopCoverPhoto()));
     }

     if (req.shopLicensePhoto() != null && !req.shopLicensePhoto().isEmpty()) {
         if (shop.getShopLicensePhoto() != null) {
             cloudinaryService.delete(shop.getShopLicensePhoto());
         }
         shop.setShopLicensePhoto(uploadIfPresent(req.shopLicensePhoto()));
     }

     return shopRepository.save(shop);
 }
 
// ===================== DELETE MY SHOP =====================
 public void deleteMyShop(User user) {

     if (!isVendor(user)) {
         throw new RuntimeException("Only vendors can delete shop");
     }

     Shop shop = shopRepository.findByUser(user)
             .orElseThrow(() -> new RuntimeException("Shop not found"));

     // 🔥 Delete images from Cloudinary
     if (shop.getShopPhoto() != null) {
         cloudinaryService.delete(shop.getShopPhoto());
     }
     if (shop.getShopCoverPhoto() != null) {
         cloudinaryService.delete(shop.getShopCoverPhoto());
     }
     if (shop.getShopLicensePhoto() != null) {
         cloudinaryService.delete(shop.getShopLicensePhoto());
     }

     shopRepository.delete(shop);
 }


 // ===================== CLOUDINARY HELPER =====================
 private String uploadIfPresent(MultipartFile file) throws IOException {
     if (file == null || file.isEmpty()) {
         return null;
     }
     return cloudinaryService.upload(file); // 🔥 ONLY CLOUDINARY
 }

 // ===================== GET MY SHOP =====================
 public ShopResponse getMyShop(User user) {
     return shopRepository.findByUser(user)
             .map(this::toResponse)
             .orElse(null);
 }

 // ===================== GET ALL APPROVED SHOPS =====================
 public List<ShopResponse> getAllShops() {
     return shopRepository.findAll().stream()
             .filter(shop -> shop.isApproved() && shop.isActive())
             .map(this::toResponse)
             .toList();
 }

 // ===================== CHECK VENDOR =====================
 private boolean isVendor(User user) {
     return user.getRole() != null && VENDOR_ROLES.contains(user.getRole().getName());
 }

 // ===================== MAP TO RESPONSE =====================
 public ShopResponse toResponse(Shop s) {
     User u = s.getUser();

     return new ShopResponse(
             s.getId(),
             s.getShopName(),
             s.getShopType(),
             s.getShopAddress(),
             s.getShopPhoto(),
             s.getShopCoverPhoto(),
             s.getShopLicensePhoto(),
             s.getWorkingHours(),
             s.getShopDescription(),
             s.getShopLicense(),
             s.isApproved(),
             s.isActive(),
             u.getId(),
             u.getName(),
             u.getPhone(),
             u.getEmail(),
             u.getRole().getName(),
             u.getPhotoUrl(),
             s.getOpensAt(),
             s.getClosesAt()
     );
 }

 
 
 //------------
 
 
//Add these methods to your existing ShopService class

public List<ShopResponse> getPopularShops() {
  return shopRepository.findPopularShops()
      .stream()
      .limit(20)
      .map(this::toResponse)
      .toList();
}

public List<ShopResponse> getTop10PopularShops() {
  return shopRepository.findPopularShops()
      .stream()
      .limit(10)
      .map(this::toResponse)
      .toList();
}

//Optional: with pagination
public List<ShopResponse> getPopularShopsPaginated(int page, int size) {
  Pageable pageable = (Pageable) PageRequest.of(page, size);
  return shopRepository.findPopularShops(pageable)
      .stream()
      .map(this::toResponse)
      .toList();
}

@Transactional
public Shop createOrUpdateShop(ShopRequest req, User user) throws IOException {
    // Only vendors allowed
    if (!isVendor(user)) {
        throw new IllegalStateException("Only vendors can create/update shop");
    }

    Optional<Shop> existing = shopRepository.findByUser(user);

    Shop shop = existing.orElseGet(() -> {
        Shop newShop = new Shop();
        newShop.setUser(user);
        newShop.setApproved(false);     // still needs admin approval
        newShop.setActive(true);
        return newShop;
    });

    // Update / set fields
    if (req.shopName()        != null) shop.setShopName(req.shopName());
    if (req.shopType()        != null) shop.setShopType(req.shopType());
    if (req.shopAddress()     != null) shop.setShopAddress(req.shopAddress());
    if (req.workingHours()    != null) shop.setWorkingHours(req.workingHours());
    if (req.shopDescription() != null) shop.setShopDescription(req.shopDescription());
    if (req.shopLicense()     != null) shop.setShopLicense(req.shopLicense());
    if (req.opensAt()         != null) shop.setOpensAt(req.opensAt());
    if (req.closesAt()        != null) shop.setClosesAt(req.closesAt());

    // Images — replace only if sent
    if (req.shopPhoto() != null && !req.shopPhoto().isEmpty()) {
        if (shop.getShopPhoto() != null) cloudinaryService.delete(shop.getShopPhoto());
        shop.setShopPhoto(cloudinaryService.upload(req.shopPhoto()));
    }
    // same for cover & license photo...

    return shopRepository.save(shop);
}
}