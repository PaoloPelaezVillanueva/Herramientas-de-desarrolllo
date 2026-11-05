package com.equipo.tambo.service;

import com.equipo.tambo.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    UserResponse getCurrentUser(String username);
    void changePassword(String username, ChangePasswordRequest request);
}
