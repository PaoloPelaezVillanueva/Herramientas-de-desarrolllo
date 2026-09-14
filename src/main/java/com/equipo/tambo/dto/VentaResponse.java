package com.equipo.tambo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VentaResponse {
    
    private Long id;
    private Long clienteId;
    private Long usuarioId;
    private LocalDateTime date;
}