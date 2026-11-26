package com.equipo.tambo.controller;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/sales", "/api/v1/ventas"})
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        return ResponseEntity.ok(saleService.listSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request,
                                                    JwtAuthenticationToken authentication,
                                                    @RequestHeader(name = "Idempotency-Key", required = false) UUID requestKey) {
        requireOwnUser(request, authentication);
        SaleResponse sale = saleService.createSale(request, requestKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable Long id, @Valid @RequestBody SaleRequest request,
                                                    JwtAuthenticationToken authentication) {
        requireOwnSale(id, authentication);
        requireOwnUser(request, authentication);
        return ResponseEntity.ok(saleService.updateSale(id, request));
    }

    private boolean isAdmin(JwtAuthenticationToken authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void requireOwnUser(SaleRequest request, JwtAuthenticationToken authentication) {
        String authenticatedUserId = authentication.getToken().getClaimAsString("userId");
        if (!isAdmin(authentication) && !String.valueOf(request.getUser()).equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La venta debe registrarse a nombre del usuario autenticado.");
        }
    }

    private void requireOwnSale(Long id, JwtAuthenticationToken authentication) {
        if (!isAdmin(authentication) && !String.valueOf(saleService.getById(id).getUser().getId())
                .equals(authentication.getToken().getClaimAsString("userId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede modificar una venta de otro usuario.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/details/{detailId}")
    public ResponseEntity<SaleResponse> updateSaleDetail(@PathVariable Long id, @PathVariable Long detailId,
                                                          @Valid @RequestBody SaleDetailRequest request,
                                                          JwtAuthenticationToken authentication) {
        requireOwnSale(id, authentication);
        return ResponseEntity.ok(saleService.updateSaleDetail(id, detailId, request));
    }

    @DeleteMapping("/{id}/details/{detailId}")
    public ResponseEntity<Void> deleteSaleDetail(@PathVariable Long id, @PathVariable Long detailId,
                                                  JwtAuthenticationToken authentication) {
        requireOwnSale(id, authentication);
        saleService.deleteSaleDetail(id, detailId);
        return ResponseEntity.noContent().build();
    }
}
