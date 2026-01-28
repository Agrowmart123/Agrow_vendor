package com.agrowmart.dto.auth.women;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record WomenProductCreateForm(
        @NotBlank(message = "Name is required") 
        @Size(max = 300) String name,

        @NotBlank(message = "Category is required") 
        String category,

        @Size(max = 2000) String description,

        @NotNull @DecimalMin("0.0") BigDecimal minPrice,
        @NotNull @DecimalMin("0.0") BigDecimal maxPrice,

        @Min(value = 0, message = "Stock cannot be negative") Integer stock,

        @NotBlank String unit,

        @Size(max = 500) String ingredients,
        @Size(max = 100) String shelfLife,
        @Size(max = 100) String packagingType,
        @Size(max = 2000) String productInfo,

        @Size(max = 8, message = "Maximum 8 images allowed") 
        List<MultipartFile> images
) {
    public WomenProductCreateDTO toCreateDTO() {
        return new WomenProductCreateDTO(
                name, category, description,
                minPrice, maxPrice, stock, unit,
                ingredients, shelfLife, packagingType, productInfo
        );
    }
}