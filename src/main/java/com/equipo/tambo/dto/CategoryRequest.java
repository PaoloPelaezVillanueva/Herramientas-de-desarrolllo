package com.equipo.tambo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "La categoría no puede superar los 100 carácteres")
    private String category;
}
