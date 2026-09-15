package com.equipo.tambo.service;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;
import com.equipo.tambo.entity.ClientEntity;
import com.equipo.tambo.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientResponse> obtenerTodos() {
        return clientRepository.findAll().stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ClientResponse obtenerPorId(Long id) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));
        return convertirAResponse(client);
    }

    public ClientResponse crear(ClientRequest request) {
        if (clientRepository.existsByDni(request.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya está registrado.");
        }
        ClientEntity guardado = clientRepository.save(convertirAEntity(request));
        return convertirAResponse(guardado);
    }

    public ClientResponse actualizar(Long id, ClientRequest request) {
        ClientEntity existente = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));

        if (clientRepository.existsByDniAndIdNot(request.getDni(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya pertenece a otro cliente.");
        }

        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setDni(request.getDni());
        existente.setEmail(request.getEmail());
        existente.setDireccion(request.getDireccion());

        return convertirAResponse(clientRepository.save(existente));
    }

    public void eliminar(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id);
        }
        clientRepository.deleteById(id);
    }

    private ClientResponse convertirAResponse(ClientEntity entity) {
        ClientResponse response = new ClientResponse();
        response.setId(entity.getId());
        response.setNombre(entity.getNombre());
        response.setApellido(entity.getApellido());
        response.setDni(entity.getDni());
        response.setEmail(entity.getEmail());
        response.setDireccion(entity.getDireccion());
        return response;
    }

    private ClientEntity convertirAEntity(ClientRequest request) {
        ClientEntity entity = new ClientEntity();
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setDni(request.getDni());
        entity.setEmail(request.getEmail());
        entity.setDireccion(request.getDireccion());
        return entity;
    }
}