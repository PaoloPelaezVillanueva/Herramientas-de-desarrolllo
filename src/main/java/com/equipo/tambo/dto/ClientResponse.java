package com.equipo.tambo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private String address;
    private String dni;
}
