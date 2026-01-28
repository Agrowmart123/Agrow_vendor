package com.agrowmart.service;

import com.agrowmart.dto.auth.product.ProductFilterDTO;
import com.agrowmart.dto.auth.women.WomenProductCreateDTO;
import com.agrowmart.dto.auth.women.WomenProductResponseDTO;
import com.agrowmart.entity.ApprovalStatus;
import com.agrowmart.entity.Product.ProductStatus;
import com.agrowmart.entity.User;
import com.agrowmart.entity.WomenProduct;
import com.agrowmart.exception.ForbiddenException;
import com.agrowmart.exception.ResourceNotFoundException;
import com.agrowmart.repository.UserRepository;
import com.agrowmart.repository.WomenProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional

public class WomenProductService {

    private final WomenProductRepository productRepo;
    private final UserRepository userRepo;
    private final CloudinaryService cloudinaryService;
    
    public WomenProductService(WomenProductRepository productRepo,UserRepository userRepo,CloudinaryService cloudinaryService ) {
        this.productRepo = productRepo;
        this.userRepo =userRepo;
        this.cloudinaryService=cloudinaryService;
    }
    
    
 // Add in WomenProductService
    private void validateSellerCanModify(Long sellerId) {
        User seller = userRepo.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!"ONLINE".equalsIgnoreCase(seller.getOnlineStatus())) {
            throw new ForbiddenException("You must set your status to ONLINE to manage products.");
        }

        if (!"YES".equals(seller.getProfileCompleted())) {
            throw new ForbiddenException("Complete your profile first before adding or modifying products.");
        }
    }
    
    
    // ========================= CREATE PRODUCT =========================
//    public WomenProductResponseDTO createProduct(Long sellerId, WomenProductCreateDTO dto, List<MultipartFile> images) throws Exception {
//    	validateSellerCanModify(sellerId);
//    	
//    	User seller = userRepo.findById(sellerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
//
//        WomenProduct product = new WomenProduct();
//        product.setSeller(seller);
//        product.setName(dto.name());
//        product.setCategory(dto.category());
//        product.setDescription(dto.description());
//     
//        product.setMinPrice(dto.minPrice());
//        product.setMaxPrice(dto.maxPrice());
//        product.setStock(dto.stock() != null ? dto.stock() : 0);
//        product.setUnit(dto.unit());
//        product.setIsAvailable(dto.stock() != null && dto.stock() > 0);
//        product.setIngredients(dto.ingredients());
//        product.setShelfLife(dto.shelfLife());
//        product.setPackagingType(dto.packagingType());
//        product.setProductInfo(dto.productInfo());
//        product.setApprovalStatus(ApprovalStatus.PENDING);
//        // Upload product images
//        List<String> uploadedUrls = uploadFiles(images);
//        product.setImageUrls(String.join(",", uploadedUrls));
//
//        product = productRepo.save(product);
//        return toDTO(product);
//    }
    
    public WomenProductResponseDTO createProduct(Long sellerId, WomenProductCreateDTO dto, List<MultipartFile> images) throws Exception {
        validateSellerCanModify(sellerId);
        
        User seller = userRepo.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        WomenProduct product = new WomenProduct();
        product.setSeller(seller);
        product.setName(dto.name());
        product.setCategory(dto.category());
        product.setDescription(dto.description());
        
        product.setMinPrice(dto.minPrice());
        product.setMaxPrice(dto.maxPrice());
        product.setStock(dto.stock() != null ? dto.stock() : 0);
        product.setUnit(dto.unit());
        product.setIsAvailable(dto.stock() != null && dto.stock() > 0);
        
        product.setIngredients(dto.ingredients());
        product.setShelfLife(dto.shelfLife());
        product.setPackagingType(dto.packagingType());
        product.setProductInfo(dto.productInfo());
        
        // ──── ADD THESE TWO LINES ────
        product.setStatus(ProductStatus.ACTIVE);              // ← Very important!
        product.setApprovalStatus(ApprovalStatus.PENDING);    // ← Already there, just keeping for clarity
        
        // Upload product images
        List<String> uploadedUrls = uploadFiles(images);
        product.setImageUrls(String.join(",", uploadedUrls));
        
        product = productRepo.save(product);
        return toDTO(product);
    }



    // ========================= GET BY ID =========================
    public WomenProductResponseDTO getProductById(Long id) {
        WomenProduct p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toDTO(p);
    }

    // ========================= DELETE PRODUCT =========================
    public void deleteProduct(Long sellerId, Long productId) {
    	validateSellerCanModify(sellerId);
    	WomenProduct p = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!p.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("You can only delete your own products");
        }

        // Delete images from Cloudinary
        if (p.getImageUrls() != null && !p.getImageUrls().isBlank()) {
            Arrays.stream(p.getImageUrls().split(","))
                    .map(this::extractPublicId)
                    .forEach(cloudinaryService::delete);
        }

        productRepo.delete(p);
    }

    
    
    
    
 // ========================= GET MY PRODUCTS (Only own products - show all statuses including PENDING)
    public List<WomenProductResponseDTO> getMyProducts(Long sellerId) {
        return productRepo.findBySellerId(sellerId).stream()
                .map(this::toDTO)
                .toList();
    }

    // New: Paginated version for /my (same logic - shows PENDING too)
    public Page<WomenProductResponseDTO> getMyProductsPaginated(Long sellerId, Pageable pageable) {
        Page<WomenProduct> productPage = productRepo.findBySellerId(sellerId, pageable);
        return productPage.map(this::toDTO);
    }

    // ========================= GET ALL PRODUCTS (PUBLIC) - ONLY APPROVED
    public List<WomenProductResponseDTO> getAllWomenProducts() {
        return productRepo.findByApprovalStatus(ApprovalStatus.APPROVED)  // ← ONLY APPROVED
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ========================= GET ALL ACTIVE (PUBLIC) - ONLY APPROVED + AVAILABLE
    public List<WomenProductResponseDTO> getAllActiveWomenProducts() {
        return productRepo.findByApprovalStatusAndIsAvailableTrue(ApprovalStatus.APPROVED)  // ← ONLY APPROVED + available
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ========================= RECENTLY ADDED (PUBLIC) - ONLY APPROVED
    public List<WomenProductResponseDTO> getRecentlyAddedWomenProducts(int limit) {
        return productRepo.findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.APPROVED)  // ← ONLY APPROVED
                .stream()
                .limit(limit)
                .map(this::toDTO)
                .toList();
    }

    // ========================= FILTERED (PUBLIC) - ONLY APPROVED
    public List<WomenProductResponseDTO> getFilteredProducts(ProductFilterDTO filter) {
        // Start with ONLY APPROVED products
        List<WomenProduct> products = productRepo.findByApprovalStatus(ApprovalStatus.APPROVED);

        // Apply other filters...
        if (filter.categories() != null && !filter.categories().isEmpty()) {
            products = products.stream()
                    .filter(p -> filter.categories().contains(p.getCategory()))
                    .toList();
        }
        if (filter.inStock() != null && filter.inStock()) {
            products = products.stream()
                    .filter(p -> p.getStock() != null && p.getStock() > 0)
                    .toList();
        }
        // Sorting...
        if (filter.sortBy() != null) {
            boolean ascending = filter.sortBy().endsWith("_low_high");
            products = products.stream()
                    .sorted((p1, p2) -> {
                        int cmp = p1.getMinPrice().compareTo(p2.getMinPrice());
                        return ascending ? cmp : -cmp;
                    })
                    .toList();
        }
        return products.stream()
                .map(this::toDTO)
                .toList();
    }
    
    
    
    // ========================= UPDATE PRODUCT =========================
//    public WomenProductResponseDTO updateProduct(Long sellerId, Long productId, WomenProductCreateDTO dto, List<MultipartFile> newImages) throws Exception {
//    	validateSellerCanModify(sellerId);
//    	WomenProduct product = productRepo.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
//
//        if (!product.getSeller().getId().equals(sellerId)) {
//            throw new SecurityException("You can only update your own products");
//        }
//
//        // Update fields
//        product.setName(dto.name());
//        product.setCategory(dto.category());
//        product.setDescription(dto.description());
//       
//        product.setMinPrice(dto.minPrice());
//        product.setMaxPrice(dto.maxPrice());
//        product.setStock(dto.stock() != null ? dto.stock() : 0);
//        product.setUnit(dto.unit());
//        product.setIsAvailable(dto.stock() != null && dto.stock() > 0);
//     // In updateProduct (preserve old value if not provided)
//        product.setIngredients(dto.ingredients() != null ? dto.ingredients() : product.getIngredients());
//        product.setShelfLife(dto.shelfLife() != null ? dto.shelfLife() : product.getShelfLife());
//        product.setPackagingType(dto.packagingType() != null ? dto.packagingType() : product.getPackagingType());
//        product.setProductInfo(dto.productInfo() != null ? dto.productInfo() : product.getProductInfo());
//        // Delete old images from Cloudinary
//        if (product.getImageUrls() != null && !product.getImageUrls().isBlank()) {
//            Arrays.stream(product.getImageUrls().split(","))
//                    .map(this::extractPublicId)
//                    .forEach(cloudinaryService::delete);
//        }
//
//        // Upload new images
//        List<String> uploadedUrls = uploadFiles(newImages);
//        product.setImageUrls(String.join(",", uploadedUrls));
//
//        product.setUpdatedAt(LocalDateTime.now());
//        product = productRepo.save(product);
//
//        return toDTO(product);
//    }
    
 // ========================= UPDATE PRODUCT =========================
    public WomenProductResponseDTO updateProduct(Long sellerId, Long productId, WomenProductCreateDTO dto, List<MultipartFile> newImages) throws Exception {
        validateSellerCanModify(sellerId);
        
        WomenProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("You can only update your own products");
        }

        // Update only if new value is provided (null-safe)
        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            product.setName(dto.name().trim());
        }

        // FIX for unit (NOT NULL in DB) – only update if provided
        if (dto.unit() != null && !dto.unit().trim().isEmpty()) {
            product.setUnit(dto.unit().trim());
        }
        // If unit is null/empty → keep old value (do NOT set null)

        if (dto.category() != null && !dto.category().trim().isEmpty()) {
            product.setCategory(dto.category().trim());
        }

        if (dto.description() != null) {
            product.setDescription(dto.description());
        }

        if (dto.minPrice() != null) {
            product.setMinPrice(dto.minPrice());
        }
        if (dto.maxPrice() != null) {
            product.setMaxPrice(dto.maxPrice());
        }
        if (dto.stock() != null) {
            product.setStock(dto.stock());
            product.setIsAvailable(dto.stock() > 0);
        }

        // Optional fields – preserve old value if not sent
        product.setIngredients(dto.ingredients() != null ? dto.ingredients() : product.getIngredients());
        product.setShelfLife(dto.shelfLife() != null ? dto.shelfLife() : product.getShelfLife());
        product.setPackagingType(dto.packagingType() != null ? dto.packagingType() : product.getPackagingType());
        product.setProductInfo(dto.productInfo() != null ? dto.productInfo() : product.getProductInfo());

     // ──── IMAGE HANDLING – FINAL SAFE LOGIC ────
        List<String> finalImageUrls = new ArrayList<>();

        if (newImages != null && !newImages.isEmpty() && newImages.stream().anyMatch(file -> !file.isEmpty())) {
            // Case 1: User sent at least one valid new image → REPLACE ALL
        	  System.out.println("New images received for product {}. Deleting old images..."+ productId);

            // Delete ALL old images from Cloudinary (only if they exist)
            if (product.getImageUrls() != null && !product.getImageUrls().isBlank()) {
                Arrays.stream(product.getImageUrls().split(","))
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .map(this::extractPublicId)
                        .forEach(publicId -> {
                            try {
                                cloudinaryService.delete(publicId);
                                System.out.println("Deleted old Cloudinary image: {}"+ publicId);
                            } catch (Exception e) {
                            	 System.out.println("Failed to delete old Cloudinary image {}: {}. Continuing..."+ publicId + e.getMessage());
                            }
                        });
            }

            // Upload only the new valid images
            List<String> uploadedUrls = uploadFiles(newImages);
            finalImageUrls.addAll(uploadedUrls);
            System.out.println("Uploaded {} new images for product {}"+ uploadedUrls.size() + productId);

        } else {
            // Case 2: No new images (or empty files) → KEEP ALL OLD IMAGES
        	 System.out.println("No new images sent for product {}. Keeping existing images."+ productId);

            if (product.getImageUrls() != null && !product.getImageUrls().isBlank()) {
                finalImageUrls = Arrays.asList(product.getImageUrls().split(","));
            }
        }

        // Set final image URLs (empty string if no images)
        product.setImageUrls(finalImageUrls.isEmpty() ? null : String.join(",", finalImageUrls));
        product.setUpdatedAt(LocalDateTime.now());

        product = productRepo.save(product);
        return toDTO(product);
    }

    // ========================= HELPER METHODS =========================

    private List<String> uploadFiles(List<MultipartFile> files) throws Exception {
        List<String> urls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    urls.add(cloudinaryService.upload(file));
                }
            }
        }
        return urls;
    }

    private String extractPublicId(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            String noExt = url.substring(0, url.lastIndexOf('.'));
            return noExt.substring(noExt.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }

    public WomenProductResponseDTO toDTO(WomenProduct p) {
        List<String> imageList = new ArrayList<>();
        if (p.getImageUrls() != null && !p.getImageUrls().isBlank()) {
            imageList = Arrays.asList(p.getImageUrls().split(","));
        }

        return new WomenProductResponseDTO(
                p.getId(),
                p.getUuid(),
                p.getSeller().getId(),
                p.getSeller().getName(),
                p.getName(),
                p.getCategory(),
                p.getDescription(),
                p.getApprovalStatus(),  // FIXED: status goes here
                p.getMinPrice(),
                p.getMaxPrice(),
                p.getStock(),
                p.getUnit(),
                imageList,
                p.getIsAvailable(),
                p.getCreatedAt(),
                p.getIngredients(),
                p.getShelfLife(),
                p.getPackagingType(),
                p.getProductInfo()
        );
    }

    
    
    
 // ===================== FILTERING - FULLY WORKING (Women Products Only) =====================
//    public List<WomenProductResponseDTO> getFilteredProducts(ProductFilterDTO filter) {
//        List<WomenProduct> products = productRepo.findAll();
//        
//
//        // Category filter
//        if (filter.categories() != null && !filter.categories().isEmpty()) {
//            products = products.stream()
//                    .filter(p -> filter.categories().contains(p.getCategory()))
//                    .toList();
//        }
//
//        // In Stock filter
//        if (filter.inStock() != null && filter.inStock()) {
//            products = products.stream()
//                    .filter(p -> p.getStock() != null && p.getStock() > 0)
//                    .toList();
//        }
//
//        // Sorting (Price & Rating)
//        if (filter.sortBy() != null) {
//            boolean ascending = filter.sortBy().endsWith("_low_high");
//            products = products.stream()
//                    .sorted((p1, p2) -> {
//                        return switch (filter.sortBy()) {
//                            case "price_low_high", "price_high_low" ->
//                                ascending ? p1.getMinPrice().compareTo(p2.getMinPrice())
//                                          : p2.getMinPrice().compareTo(p1.getMinPrice());
//       //                  case "rating_low_high", "rating_high_low" -> {
////                                BigDecimal r1 = p1.getAverageRating() != null ? p1.getAverageRating() : BigDecimal.ZERO;
////                                BigDecimal r2 = p2.getAverageRating() != null ? p2.getAverageRating() : BigDecimal.ZERO;
////                               yield ascending ? r1.compareTo(r2) : r2.compareTo(r1);
//   //                        }
//                            default -> 0;
//                        };
//                    })
//                    .toList();
//        }
//
//        return products.stream()
//                .map(this::toDTO)
//                .toList();
//    }
//    

    
    

    
 //--------------------
    
 // Add to WomenProductService

//    public List<WomenProductResponseDTO> getRecentlyAddedWomenProducts(int limit) {
//        return productRepo.findAllByOrderByCreatedAtDesc()
//            .stream()
//            .limit(limit)
//            .map(this::toDTO)
//            .toList();
//    }
//    
    
 //status
    
 // Add this method
    // Add this method
    public WomenProductResponseDTO updateWomenProductStatus(Long productId, boolean isActive, Long sellerId) {
        validateSellerCanModify(sellerId);

        WomenProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ForbiddenException("You can only update status of your own products");
        }

        // Optional: skip if no change (idempotent)
        if (product.getIsAvailable() != isActive) {
            product.setIsAvailable(isActive);
            product.setUpdatedAt(LocalDateTime.now());
            product = productRepo.save(product);
        }

        return toDTO(product);
    }
    public WomenProductResponseDTO updateProductApproval(
            Long productId,
            ApprovalStatus status
    ) {
        WomenProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setApprovalStatus(status);

        if (status == ApprovalStatus.APPROVED) {
            product.setIsAvailable(true);
        } else {
            product.setIsAvailable(false);
        }

        product.setUpdatedAt(LocalDateTime.now());
        productRepo.save(product);

        return toDTO(product);
    }
    
  //Deepti Kadam
    // ===================== ADMIN METHODS =====================
       public List<WomenProduct> getAllProductsForAdmin() {
           return productRepo.findAllByOrderByCreatedAtDesc();
       }


       public WomenProduct getProductByIdForAdmin(Long id) {
           return productRepo.findById(id)
                   .orElseThrow(() -> new RuntimeException("Women product not found"));
       }

}