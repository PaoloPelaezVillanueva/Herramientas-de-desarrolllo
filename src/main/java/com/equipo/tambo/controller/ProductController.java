package com.equipo.tambo.controller;

import com.equipo.tambo.dto.ProductRequest;
import com.equipo.tambo.dto.ProductResponse;
import com.equipo.tambo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listar() {
        return ResponseEntity.ok(productService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(productService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> crear(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse productoCreado = productService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(
                productService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        productService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}