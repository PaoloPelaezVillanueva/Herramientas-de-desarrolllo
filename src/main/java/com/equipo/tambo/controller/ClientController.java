package com.equipo.tambo.controller;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;
import com.equipo.tambo.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listarTodos() {
        return ResponseEntity.ok(clientService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> crear(@Valid @RequestBody ClientRequest request) {
        ClientResponse nuevoCliente = clientService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clientService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
