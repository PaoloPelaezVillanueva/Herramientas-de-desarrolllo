package com.equipo.tambo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty(message = "La venta debe tener al menos un detalle")
    private List<@NotNull(message = "El detalle de la venta no puede ser nulo") SaleDetailRequest> details;
}
