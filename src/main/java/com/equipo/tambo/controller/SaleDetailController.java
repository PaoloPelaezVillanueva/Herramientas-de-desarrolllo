package com.equipo.tambo.controller;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleDetailResponse;
import com.equipo.tambo.service.SaleDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-detail")
@RequiredArgsConstructor
public class SaleDetailController {

    private final SaleDetailService saleDetailService;

    @GetMapping
    public ResponseEntity<List<SaleDetailResponse>> getAllSalesDetail() {
        return ResponseEntity.ok(saleDetailService.listSalesDetail());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDetailResponse> getSaleDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(saleDetailService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SaleDetailResponse> createSaleDetail(@Valid @RequestBody SaleDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleDetailService.createSaleDetail(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleDetailResponse> updateSaleDetail(@PathVariable Long id, @Valid @RequestBody SaleDetailRequest request) {
        return ResponseEntity.ok(saleDetailService.updateSaleDetail(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleDetail(@PathVariable Long id) {
        saleDetailService.deleteSaleDetail(id);

        return ResponseEntity.noContent().build();
    }
}