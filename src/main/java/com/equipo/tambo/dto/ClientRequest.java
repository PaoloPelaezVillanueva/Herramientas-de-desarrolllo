package com.equipo.tambo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String lastname;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String email;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String address;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "[0-9]{8,10}", message = "El DNI debe tener entre 8 y 10 dígitos")
    private String dni;
}
