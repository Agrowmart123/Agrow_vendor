package com.agrowmart.dto.auth.shop;

import jakarta.validation.constraints.Min;

public record ShopSearchDTO(
        String keyword,        // shop name
        String city,
        String pincode,
        Double minRating,
        @Min(0) int page,
        @Min(1) int size
) {}