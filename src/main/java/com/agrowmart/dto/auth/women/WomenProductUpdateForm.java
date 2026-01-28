package com.agrowmart.dto.auth.women;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record WomenProductUpdateForm(
        @Size(max = 300) String name,
        String category,
        @Size(max = 2000) String description,
        @DecimalMin("0.0") BigDecimal minPrice,
        @DecimalMin("0.0") BigDecimal maxPrice,
        @Min(0) Integer stock,
        String unit,
        @Size(max = 500) String ingredients,
        @Size(max = 100) String shelfLife,
        @Size(max = 100) String packagingType,
        @Size(max = 2000) String productInfo,
        @Size(max = 8) List<MultipartFile> images
) {
    public WomenProductCreateDTO toCreateDTO() {
        return new WomenProductCreateDTO(
                name, category, description,
                minPrice, maxPrice, stock, unit,
                ingredients, shelfLife, packagingType, productInfo
        );
    }
}