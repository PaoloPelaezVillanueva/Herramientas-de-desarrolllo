package com.equipo.tambo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String direccion;
}
