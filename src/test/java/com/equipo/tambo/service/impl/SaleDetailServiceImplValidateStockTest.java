package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleDetailServiceImplValidateStockTest {
    @Mock SaleDetailRepository saleDetailRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks SaleDetailServiceImpl service;

    private SaleDetailRequest req(Long product, Long qty) {
        SaleDetailRequest r = new SaleDetailRequest();
        r.setProduct(product);
        r.setQuantity(qty);
        return r;
    }

    private Product product(int stock) {
        Product p = new Product();
        p.setName("Leche");
        p.setStock(stock);
        return p;
    }

    @Test
    void passesWhenStockIsEnough() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(10)));

        assertDoesNotThrow(() -> service.validateStock(List.of(req(1L, 10L))));
    }

    @Test
    void sumsRepeatedProductsBeforeComparing() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(10)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateStock(List.of(req(1L, 6L), req(1L, 5L))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Stock disponible: 10"));
        assertTrue(ex.getReason().contains("Cantidad solicitada: 11"));
    }

    @Test
    void throwsNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateStock(List.of(req(7L, 1L))));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void validatesEveryDistinctProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(5)));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product(1)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateStock(List.of(req(1L, 2L), req(2L, 3L))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void acceptsRepeatedProductsAtExactStockWithoutChangingInventory() {
        Product product = product(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        service.validateStock(List.of(req(1L, 4L), req(1L, 6L)));

        assertEquals(10, product.getStock());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(saleDetailRepository);
    }

    @Test
    void rejectsPositiveQuantityWhenStockIsZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(0)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateStock(List.of(req(1L, 1L))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void emptyDetailsDoNotAccessRepositories() {
        service.validateStock(List.of());

        verifyNoInteractions(productRepository, saleDetailRepository);
    }
}
