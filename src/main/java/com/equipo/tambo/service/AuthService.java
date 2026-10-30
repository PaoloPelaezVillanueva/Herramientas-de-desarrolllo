package com.equipo.tambo.service;

import com.equipo.tambo.dto.AuthResponse;
import com.equipo.tambo.dto.LoginRequest;
import com.equipo.tambo.dto.RegisterRequest;
import com.equipo.tambo.dto.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
}
