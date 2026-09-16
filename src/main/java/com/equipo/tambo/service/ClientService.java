package com.equipo.tambo.service;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;
import com.equipo.tambo.entity.ClientEntity;
import com.equipo.tambo.entity.UserEntity;
import com.equipo.tambo.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientResponse> listClients() {
        return clientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse getById(Long id) {
        return toResponse(getClient(id));
    }

    private ClientEntity getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el cliente con ID " + id
                ));
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (clientRepository.existsByDni(request.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya está registrado.");
        }

        ClientEntity client = new ClientEntity();
        client.setName(request.getName());
        client.setLastname(request.getLastname());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        client.setDni(request.getDni());

        return toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        ClientEntity client = getClient(id);

        if (clientRepository.existsByDniAndIdNot(request.getDni(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya pertenece a otro cliente.");
        }

        client.setName(request.getName());
        client.setLastname(request.getLastname());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        client.setDni(request.getDni());

        return toResponse(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        ClientEntity client = getClient(id);
        clientRepository.delete(client);
    }

    private ClientResponse toResponse(ClientEntity client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getLastname(),
                client.getEmail(),
                client.getAddress(),
                client.getDni()
        );
    }
}