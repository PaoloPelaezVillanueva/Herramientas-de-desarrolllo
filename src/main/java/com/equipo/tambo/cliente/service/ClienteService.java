package com.equipo.tambo.cliente.service;

import com.equipo.tambo.cliente.dto.ClienteRequest;
import com.equipo.tambo.cliente.dto.ClienteResponse;
import com.equipo.tambo.cliente.entity.ClienteEntity;
import com.equipo.tambo.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        return convertirAResponse(cliente);
    }

    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI " + request.getDni() + " ya está registrado.");
        }
        ClienteEntity entity = convertirAEntity(request);
        ClienteEntity guardado = clienteRepository.save(entity);
        return convertirAResponse(guardado);
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        ClienteEntity existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        if (clienteRepository.existsByDniAndIdNot(request.getDni(), id)) {
            throw new IllegalArgumentException("El DNI " + request.getDni() + " ya pertenece a otro cliente.");
        }

        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setDni(request.getDni());
        existente.setEmail(request.getEmail());
        existente.setTelefono(request.getTelefono());

        ClienteEntity actualizado = clienteRepository.save(existente);
        return convertirAResponse(actualizado);
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con id: " + id);
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
        response.setTelefono(entity.getTelefono());
        return response;
    }

    private ClienteEntity convertirAEntity(ClienteRequest request) {
        ClienteEntity entity = new ClienteEntity();
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setDni(request.getDni());
        entity.setEmail(request.getEmail());
        entity.setTelefono(request.getTelefono());
        return entity;
    }
}
