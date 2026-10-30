package com.equipo.tambo.service;

import com.equipo.tambo.security.CustomUserDetails;

public interface JwtService {
    String generateToken(CustomUserDetails userDetails);
    long getExpirationSeconds();
}
