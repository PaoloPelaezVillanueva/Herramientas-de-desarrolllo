package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;
import com.equipo.tambo.entity.Client;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.service.ClientService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<ClientResponse> listClients() {
        return clientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponse getById(Long id) {
        return toResponse(getClient(id));
    }

    private Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el cliente con ID " + id
                ));
    }

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (clientRepository.existsByDni(request.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya está registrado.");
        }
        requireAvailableEmail(clientRepository.existsByEmailIgnoreCase(request.getEmail()));

        Client client = new Client();
        return saveClient(client, request);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = getClient(id);

        if (clientRepository.existsByDniAndIdNot(request.getDni(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI " + request.getDni() + " ya pertenece a otro cliente.");
        }
        requireAvailableEmail(clientRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id));

        return saveClient(client, request);
    }

    private void requireAvailableEmail(boolean exists) {
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya pertenece a otro cliente.");
        }
    }

    private ClientResponse saveClient(Client client, ClientRequest request) {
        client.setName(request.getName());
        client.setLastname(request.getLastname());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress() == null ? "" : request.getAddress());
        client.setDni(request.getDni());

        try {
            return toResponse(clientRepository.saveAndFlush(client));
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo o DNI ya está registrado.");
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = getClient(id);
        try {
            clientRepository.delete(client);
            clientRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el cliente porque tiene ventas asociadas.", ex);
        }
    }

    private ClientResponse toResponse(Client client) {
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
