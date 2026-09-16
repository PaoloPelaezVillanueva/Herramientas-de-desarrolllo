package com.equipo.tambo.controller;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;
import com.equipo.tambo.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.listClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        ClientResponse client = clientService.createClient(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        clientService.deleteClient(id);

        return ResponseEntity.noContent().build();
    }
}
