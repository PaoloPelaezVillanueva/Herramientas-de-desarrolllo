package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.entity.SaleDetail;
import com.equipo.tambo.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleDetailServiceImplTest {
    @Mock ProductRepository productRepository;
    @Mock EntityManager entityManager;

    private SaleDetailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleDetailServiceImpl(productRepository, entityManager);
    }

    @Test
    void rejectsTwentyUnitsWhenOnlyEighteenAreAvailable() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Bebida");
        product.setStock(18);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        SaleDetailRequest request = new SaleDetailRequest();
        request.setProduct(1L);
        request.setQuantity(20L);
        Sale sale = new Sale();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.replaceDetails(sale, List.of(request)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(18, product.getStock());
        assertTrue(sale.getDetails().isEmpty());
    }

    @Test
    void rejectsSaleOfInactiveProductWithoutChangingStock() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Bebida retirada");
        product.setStock(18);
        product.setActive(false);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        SaleDetailRequest request = new SaleDetailRequest();
        request.setProduct(1L);
        request.setQuantity(1L);
        Sale sale = new Sale();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.replaceDetails(sale, List.of(request)));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(18, product.getStock());
        assertTrue(sale.getDetails().isEmpty());
    }

    @Test
    void editingAnExistingSaleKeepsTheOriginalPriceAndStock() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(16);
        product.setCost(new BigDecimal("3.50"));
        Sale sale = new Sale();
        SaleDetail detail = new SaleDetail();
        detail.setProduct(product);
        detail.setQuantity(2L);
        detail.setSubtotal(new BigDecimal("5.00"));
        sale.addDetail(detail);

        SaleDetailRequest request = new SaleDetailRequest();
        request.setProduct(1L);
        request.setQuantity(2L);
        service.replaceDetails(sale, List.of(request));

        assertEquals(16, product.getStock());
        assertEquals(new BigDecimal("5.00"), sale.getDetails().getFirst().getSubtotal());
    }
}
