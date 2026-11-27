package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.Client;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.entity.SaleDetail;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.repository.SaleRepository;
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

import com.equipo.tambo.service.SaleDetailService;
import java.math.BigDecimal;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplCreateTest {
    @Mock SaleRepository saleRepository;
    @Mock UserRepository userRepository;
    @Mock ClientRepository clientRepository;
    @Mock SaleDetailService saleDetailService;
    @InjectMocks SaleServiceImpl service;

    @Test
    void createSaleStopsWhenStockValidationFails() {
        SaleRequest request = new SaleRequest();
        request.setUser(1L);
        SaleDetailRequest detail = new SaleDetailRequest();
        detail.setProduct(2L);
        detail.setQuantity(3L);
        request.setDetails(List.of(detail));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente"))
                .when(saleDetailService).validateStock(request.getDetails());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSale(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(saleDetailService, never()).createSaleDetail(any(), any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void createSaleValidatesStockBeforeCreatingDetailsAndSaving() {
        User user = new User();
        Client client = new Client();
        SaleDetailRequest first = new SaleDetailRequest();
        first.setProduct(2L);
        first.setQuantity(3L);
        SaleDetailRequest second = new SaleDetailRequest();
        second.setProduct(4L);
        second.setQuantity(1L);
        SaleRequest request = new SaleRequest();
        request.setUser(1L);
        request.setClient(5L);
        request.setDetails(List.of(first, second));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));
        doAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            SaleDetailRequest detailRequest = invocation.getArgument(1);
            SaleDetail detail = new SaleDetail();
            detail.setProduct(new Product());
            detail.setQuantity(detailRequest.getQuantity());
            detail.setSubtotal(new BigDecimal("2.50").multiply(BigDecimal.valueOf(detailRequest.getQuantity())));
            sale.addDetail(detail);
            return null;
        }).when(saleDetailService).createSaleDetail(any(Sale.class), any(SaleDetailRequest.class));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            sale.setId(10L);
            return sale;
        });

        SaleResponse response = service.createSale(request);

        InOrder order = inOrder(saleDetailService, saleRepository);
        order.verify(saleDetailService).validateStock(request.getDetails());
        order.verify(saleDetailService).createSaleDetail(any(Sale.class), same(first));
        order.verify(saleDetailService).createSaleDetail(any(Sale.class), same(second));
        order.verify(saleRepository).save(any(Sale.class));
        assertEquals(10L, response.getId());
        assertSame(user, response.getUser());
        assertSame(client, response.getClient());
        assertNotNull(response.getDate());
        assertEquals(new BigDecimal("10.00"), response.getTotal());
        assertEquals(2, response.getDetails().size());
    }
}
