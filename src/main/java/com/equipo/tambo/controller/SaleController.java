package com.equipo.tambo.controller;

import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<List<SaleResponse>> listar() {
        return ResponseEntity.ok(saleService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(saleService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SaleResponse> crear(
            @Valid @RequestBody SaleRequest request
    ) {
        SaleResponse ventaCreada = saleService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ventaCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequest request
    ) {
        return ResponseEntity.ok(
                saleService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        saleService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}