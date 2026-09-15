package com.equipo.tambo.detalleventa.controller;

import com.equipo.tambo.detalleventa.dto.DetalleVentaRequest;
import com.equipo.tambo.detalleventa.dto.DetalleVentaResponse;
import com.equipo.tambo.detalleventa.service.DetalleVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
@RequiredArgsConstructor
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    @GetMapping
    public ResponseEntity<List<DetalleVentaResponse>> listar() {
        return ResponseEntity.ok(detalleVentaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleVentaResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                detalleVentaService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<DetalleVentaResponse> crear(
            @Valid @RequestBody DetalleVentaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(detalleVentaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleVentaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DetalleVentaRequest request
    ) {
        return ResponseEntity.ok(
                detalleVentaService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        detalleVentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}