package com.equipo.tambo.cliente.service;

import com.equipo.tambo.cliente.dto.ClienteRequest;
import com.equipo.tambo.cliente.dto.ClienteResponse;
import com.equipo.tambo.cliente.entity.ClienteEntity;
import com.equipo.tambo.cliente.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponse> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ClienteResponse obtenerPorId(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));
        return convertirAResponse(cliente);
    }

    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya está registrado.");
        }
        ClienteEntity guardado = clienteRepository.save(convertirAEntity(request));
        return convertirAResponse(guardado);
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        ClienteEntity existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));

        if (clienteRepository.existsByDniAndIdNot(request.getDni(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya pertenece a otro cliente.");
        }

        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setDni(request.getDni());
        existente.setEmail(request.getEmail());
        existente.setDireccion(request.getDireccion());

        return convertirAResponse(clienteRepository.save(existente));
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteResponse convertirAResponse(ClienteEntity entity) {
        ClienteResponse response = new ClienteResponse();
        response.setId(entity.getId());
        response.setNombre(entity.getNombre());
        response.setApellido(entity.getApellido());
        response.setDni(entity.getDni());
        response.setEmail(entity.getEmail());
        response.setDireccion(entity.getDireccion());
        return response;
    }

    private ClienteEntity convertirAEntity(ClienteRequest request) {
        ClienteEntity entity = new ClienteEntity();
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setDni(request.getDni());
        entity.setEmail(request.getEmail());
        entity.setDireccion(request.getDireccion());
        return entity;
    }
}