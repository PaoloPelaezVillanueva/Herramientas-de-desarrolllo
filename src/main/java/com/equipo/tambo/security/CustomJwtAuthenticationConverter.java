package com.equipo.tambo.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter authoritiesConverter;

    public CustomJwtAuthenticationConverter() {
        this.authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        this.authoritiesConverter.setAuthoritiesClaimName("roles");
        this.authoritiesConverter.setAuthorityPrefix("");
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

        return new JwtAuthenticationToken(jwt, authorities, Objects.requireNonNull(jwt.getSubject()));
    }
}
