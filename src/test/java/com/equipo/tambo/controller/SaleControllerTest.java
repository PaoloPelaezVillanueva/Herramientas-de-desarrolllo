package com.equipo.tambo.controller;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.service.SaleService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SaleControllerTest {
    private final SaleService saleService = mock(SaleService.class);
    private final SaleController controller = new SaleController(saleService);

    @Test
    void cashierCannotCreateSaleInAnotherUsersName() {
        SaleRequest request = new SaleRequest();
        request.setUser(2L);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.createSale(request, authentication(1L, "ROLE_USER"), null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(saleService);
    }

    @Test
    void cashierCannotEditAnotherUsersSale() {
        User owner = new User();
        owner.setId(2L);
        when(saleService.getById(7L)).thenReturn(new SaleResponse(7L, null, owner, null, null, List.of()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.updateSaleDetail(7L, 3L, new SaleDetailRequest(), authentication(1L, "ROLE_USER")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(saleService, never()).updateSaleDetail(anyLong(), anyLong(), any());
    }

    @Test
    void cashierCanCreateOwnSale() {
        SaleRequest request = new SaleRequest();
        request.setUser(1L);

        controller.createSale(request, authentication(1L, "ROLE_USER"), null);

        verify(saleService).createSale(request, null);
    }

    private JwtAuthenticationToken authentication(Long userId, String role) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("cashier")
                .claim("userId", userId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)));
    }
}
