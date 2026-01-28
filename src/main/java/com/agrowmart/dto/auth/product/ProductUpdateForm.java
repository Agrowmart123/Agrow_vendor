package com.agrowmart.dto.auth.product;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateForm(
        String productName,
        String shortDescription,
        Long categoryId,
        @Min(value = 0, message = "Stock cannot be negative") Double stockQuantity,
        String shelfLife,

        // Vegetable
        String vegWeight,
        String vegUnit,
        @DecimalMin("0.0") BigDecimal vegMinPrice,
        @DecimalMin("0.0") BigDecimal vegMaxPrice,
        String vegDisclaimer,

        // Dairy
        String dairyQuantity,
        String dairyBrand,
        String dairyIngredients,
        String dairyPackagingType,
        String dairyProductInfo,
        String dairyUsageInfo,
        String dairyUnit,
        String dairyStorage,
        @DecimalMin("0.0") BigDecimal dairyMinPrice,
        @DecimalMin("0.0") BigDecimal dairyMaxPrice,

        // Meat
        String meatQuantity,
        String meatBrand,
        String meatKeyFeatures,
        String meatCutType,
        String meatServingSize,
        String meatStorageInstruction,
        String meatUsage,
        String meatEnergy,
        Boolean meatMarinated,
        String meatPackagingType,
        String meatDisclaimer,
        String meatRefundPolicy,
        @DecimalMin("0.0") BigDecimal meatMinPrice,
        @DecimalMin("0.0") BigDecimal meatMaxPrice,

        List<MultipartFile> images,
        List<String> removeImageUrls
) {
    public ProductUpdateDTO toServiceDTO() {
        return new ProductUpdateDTO(
                productName, shortDescription, categoryId,
                images, removeImageUrls, shelfLife, stockQuantity,
                vegWeight, vegUnit, vegMinPrice, vegMaxPrice, vegDisclaimer,
                dairyQuantity, dairyBrand, dairyIngredients, dairyPackagingType,
                dairyProductInfo, dairyUsageInfo, dairyUnit, dairyStorage,
                dairyMinPrice, dairyMaxPrice,
                meatQuantity, meatBrand, meatKeyFeatures, meatCutType,
                meatServingSize, meatStorageInstruction, meatUsage, meatEnergy,
                meatMarinated, meatPackagingType, meatDisclaimer, meatRefundPolicy,
                meatMinPrice, meatMaxPrice
        );
    }
}