package com.equipo.tambo.service;

import com.equipo.tambo.dto.ClientRequest;
import com.equipo.tambo.dto.ClientResponse;

import java.util.List;

public interface ClientService {
    List<ClientResponse> listClients();
    ClientResponse getById(Long id);
    ClientResponse createClient(ClientRequest request);
    ClientResponse updateClient(Long id, ClientRequest request);
    void deleteClient(Long id);
}
