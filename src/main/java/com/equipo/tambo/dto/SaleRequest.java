package com.equipo.tambo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SaleRequest {
    
    private Long client;
    
    @NotNull(message = "El ID del usuario/cajero es obligatorio")
    private Long user;

    @Valid
    @NotNull(message = "Los detalles de la venta son obligatorias")
    private List<SaleDetailRequest> details;
}