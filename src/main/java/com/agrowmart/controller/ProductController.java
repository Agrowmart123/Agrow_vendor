
package com.agrowmart.controller;

import com.agrowmart.dto.auth.product.*;
import com.agrowmart.entity.User;
import com.agrowmart.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ===================== CREATE PRODUCT =====================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ProductResponseDTO> create(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute ProductCreateDTO dto) throws Exception {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Vendor {} creating product: {}", user.getId(), dto.productName());

        ProductResponseDTO created = productService.create(dto, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===================== UPDATE PRODUCT =====================
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute ProductUpdateDTO dto) throws Exception {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Vendor {} updating product {}", user.getId(), id);

        ProductResponseDTO updated = productService.update(id, dto, user.getId());

        return ResponseEntity.ok(updated);
    }

    // ===================== DELETE PRODUCT =====================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        productService.delete(id, user.getId());
        log.info("Vendor {} deleted product {}", user.getId(), id);

        return ResponseEntity.noContent().build();
    }

    // ===================== VENDOR'S OWN PRODUCTS (Paginated) =====================
    @GetMapping("/vendor")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<VendorProductPaginatedResponse> getVendorProducts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "serialNo") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String status) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        VendorProductPaginatedResponse response = productService.getVendorProductsPaginated(
                user.getId(), page, size, status
        );

        return ResponseEntity.ok(response);
    }

    // ===================== UPDATE PRODUCT STATUS =====================
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ProductResponseDTO> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusUpdateRequest request,
            @AuthenticationPrincipal User user) throws Exception {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProductResponseDTO updated = productService.updateStatus(
                id,
                request.status().toUpperCase(),
                user.getId()
        );

        return ResponseEntity.ok(updated);
    }

    // ===================== PUBLIC ENDPOINTS (unchanged) =====================
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getProductsByShop(shopId));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(productService.getPublicProductById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> search(@Valid @RequestBody ProductSearchDTO filter) {
        return ResponseEntity.ok(productService.search(filter));
    }
}