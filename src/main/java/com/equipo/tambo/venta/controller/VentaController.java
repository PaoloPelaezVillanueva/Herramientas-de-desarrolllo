package com.equipo.tambo.venta.controller;

import com.equipo.tambo.venta.dto.VentaRequest;
import com.equipo.tambo.venta.dto.VentaResponse;
import com.equipo.tambo.venta.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ventaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VentaResponse> crear(
            @Valid @RequestBody VentaRequest request
    ) {
        VentaResponse ventaCreada = ventaService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ventaCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VentaRequest request
    ) {
        return ResponseEntity.ok(
                ventaService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}