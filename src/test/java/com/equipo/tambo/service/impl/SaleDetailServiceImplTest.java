package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleDetailServiceImplTest {
    @Mock ProductRepository productRepository;

    private SaleDetailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleDetailServiceImpl(productRepository);
    }

    @Test
    void rejectsTwentyUnitsWhenOnlyEighteenAreAvailable() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Bebida");
        product.setStock(18);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        SaleDetailRequest request = new SaleDetailRequest();
        request.setProduct(1L);
        request.setQuantity(20L);
        Sale sale = new Sale();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.createSaleDetail(sale, request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(18, product.getStock());
        assertTrue(sale.getDetails().isEmpty());
        verify(productRepository, never()).save(product);
    }

    @Test
    void rejectsSaleOfInactiveProductWithoutChangingStock() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Bebida retirada");
        product.setStock(18);
        product.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        SaleDetailRequest request = new SaleDetailRequest();
        request.setProduct(1L);
        request.setQuantity(1L);
        Sale sale = new Sale();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.createSaleDetail(sale, request));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(18, product.getStock());
        assertTrue(sale.getDetails().isEmpty());
        verify(productRepository, never()).save(product);
    }
}
