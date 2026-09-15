package com.equipo.tambo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
@RequiredArgsConstructor
public class SaleDetailController {

    private final com.equipo.tambo.service.SaleDetailService saleDetailService;

    @GetMapping
    public ResponseEntity<List<com.equipo.tambo.dto.SaleDetailResponse>> listar() {
        return ResponseEntity.ok(saleDetailService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.equipo.tambo.dto.SaleDetailResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                saleDetailService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<com.equipo.tambo.dto.SaleDetailResponse> crear(
            @Valid @RequestBody com.equipo.tambo.dto.SaleDetailRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleDetailService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<com.equipo.tambo.dto.SaleDetailResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody com.equipo.tambo.dto.SaleDetailRequest request
    ) {
        return ResponseEntity.ok(
                saleDetailService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        saleDetailService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}