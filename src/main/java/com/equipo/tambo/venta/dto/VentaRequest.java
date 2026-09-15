package com.equipo.tambo.venta.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VentaRequest {
    
    private Long clienteId; 
    
    @NotNull(message = "El ID del usuario/cajero es obligatorio")
    private Long usuarioId; 
}