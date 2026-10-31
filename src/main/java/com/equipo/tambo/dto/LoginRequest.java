package com.equipo.tambo.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String user,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
