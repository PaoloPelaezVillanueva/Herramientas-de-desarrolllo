package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Product;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
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
class ProductServiceImplDeleteTest {
    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock SaleDetailRepository saleDetailRepository;
    @InjectMocks ProductServiceImpl service;

    @Test
    void deleteRemovesEntityWhenNoRelatedRecords() {
        Product entity = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleDetailRepository.existsByProductId(1L)).thenReturn(false);

        service.deleteProduct(1L);

        verify(productRepository).delete(entity);
    }

    @Test
    void deleteThrowsConflictWhenRelatedRecordsExist() {
        Product entity = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(saleDetailRepository.existsByProductId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteProduct(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(productRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteProduct(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository, never()).delete(any());
    }
}
