
package com.agrowmart.controller;

import com.agrowmart.dto.auth.women.*;
import com.agrowmart.entity.User;
import com.agrowmart.service.WomenProductService;
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
@RequestMapping("/api/women-products")
public class WomenProductController {

    private static final Logger log = LoggerFactory.getLogger(WomenProductController.class);

    private final WomenProductService service;

    public WomenProductController(WomenProductService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('WOMEN')")
    public ResponseEntity<WomenProductResponseDTO> create(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute WomenProductCreateForm request) throws Exception {

        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        log.info("Women seller {} creating product: {}", user.getId(), request.name());

        WomenProductResponseDTO response = service.createProduct(
                user.getId(),
                request.toCreateDTO(),
                request.images() != null ? request.images() : List.of()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('WOMEN')")
    public ResponseEntity<Page<WomenProductResponseDTO>> getMyProducts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sortBy)
        );

        Page<WomenProductResponseDTO> products = service.getMyProductsPaginated(user.getId(), pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping
    public ResponseEntity<List<WomenProductResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllWomenProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WomenProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('WOMEN')")
    public ResponseEntity<WomenProductResponseDTO> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @ModelAttribute WomenProductUpdateForm request) throws Exception {

        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        log.info("Women seller {} updating product {}", user.getId(), id);

        WomenProductResponseDTO updated = service.updateProduct(
                user.getId(),
                id,
                request.toCreateDTO(),
                request.images() != null ? request.images() : List.of()
        );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WOMEN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        service.deleteProduct(user.getId(), id);
        log.info("Women seller {} deleted product {}", user.getId(), id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('WOMEN')")
    public ResponseEntity<WomenProductResponseDTO> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody WomenProductStatusRequest request,
            @AuthenticationPrincipal User user) {

        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean isActive = "ACTIVE".equalsIgnoreCase(request.status());
        WomenProductResponseDTO updated = service.updateWomenProductStatus(id, isActive, user.getId());

        return ResponseEntity.ok(updated);
    }
}