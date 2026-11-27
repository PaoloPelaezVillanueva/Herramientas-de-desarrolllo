package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.User;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.repository.RoleRepository;
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
class UserServiceImplDeleteTest {
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock SaleRepository saleRepository;
    @InjectMocks UserServiceImpl service;

    @Test
    void deleteRemovesEntityWhenNoRelatedRecords() {
        User entity = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleRepository.existsByUserId(1L)).thenReturn(false);

        service.deleteUser(1L);

        verify(userRepository).delete(entity);
    }

    @Test
    void deleteThrowsConflictWhenRelatedRecordsExist() {
        User entity = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleRepository.existsByUserId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteUser(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteUser(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(userRepository, never()).delete(any());
    }
}
