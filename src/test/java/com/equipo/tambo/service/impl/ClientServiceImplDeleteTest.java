package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Client;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplDeleteTest {
    @Mock ClientRepository clientRepository;
    @Mock SaleRepository saleRepository;
    @InjectMocks ClientServiceImpl service;

    @Test
    void deleteRemovesEntityWhenNoRelatedRecords() {
        Client entity = new Client();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleRepository.existsByClientId(1L)).thenReturn(false);

        service.deleteClient(1L);

        verify(clientRepository).delete(entity);
    }

    @Test
    void deleteThrowsConflictWhenRelatedRecordsExist() {
        Client entity = new Client();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleRepository.existsByClientId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteClient(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(clientRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteClient(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(clientRepository, never()).delete(any());
    }
}
