package com.equipo.tambo.service.impl;

import com.equipo.tambo.security.CustomUserDetails;
import com.equipo.tambo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    @Override
    public String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tambo-api")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("name", userDetails.getName())
                .claim("lastname", userDetails.getLastname())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long getExpirationSeconds() {
        return ChronoUnit.MINUTES.getDuration()
                .multipliedBy(expirationMinutes)
                .toSeconds();
    }
}
