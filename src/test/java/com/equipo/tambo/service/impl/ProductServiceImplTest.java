package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Product;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock SaleDetailRepository saleDetailRepository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productRepository, categoryRepository, saleDetailRepository);
    }

    @Test
    void rejectsDeletingProductUsedInSale() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(saleDetailRepository.existsByProductId(1L)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteProduct(1L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(productRepository, never()).delete(product);
    }

    @Test
    void deletesProductWithoutSales() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        service.deleteProduct(1L);

        verify(saleDetailRepository).existsByProductId(1L);
        verify(productRepository).delete(product);
    }
}
