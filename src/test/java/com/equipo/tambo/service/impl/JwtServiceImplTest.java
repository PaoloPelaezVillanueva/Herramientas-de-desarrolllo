package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {
    @Test
    void tokenIncludesUserClaimsRoleAndConfiguredExpiry() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        JwtServiceImpl service = new JwtServiceImpl(encoder);
        ReflectionTestUtils.setField(service, "expirationMinutes", 30L);
        Jwt token = mock(Jwt.class);
        when(token.getTokenValue()).thenReturn("encoded-jwt");
        when(encoder.encode(any())).thenReturn(token);
        User user = new User(7L, "Paolo", "Pelaez", "paolo", "hash", new Role(2L, "USER"));

        Instant before = Instant.now();
        String value = service.generateToken(new CustomUserDetails(user));
        Instant after = Instant.now();

        assertEquals("encoded-jwt", value);
        assertEquals(1800L, service.getExpirationSeconds());
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var parameters = captor.getValue();
        var claims = parameters.getClaims();
        assertEquals("HS256", parameters.getJwsHeader().getAlgorithm().getName());
        assertEquals("tambo-api", claims.getClaim("iss"));
        assertEquals("paolo", claims.getSubject());
        assertEquals(Long.valueOf(7L), claims.getClaim("userId"));
        assertEquals("Paolo", claims.getClaim("name"));
        assertEquals("Pelaez", claims.getClaim("lastname"));
        assertEquals(java.util.List.of("ROLE_USER"), claims.getClaim("roles"));
        assertFalse(claims.getIssuedAt().isBefore(before));
        assertFalse(claims.getIssuedAt().isAfter(after));
        assertEquals(Duration.ofMinutes(30), Duration.between(claims.getIssuedAt(), claims.getExpiresAt()));
    }
}
