package com.equipo.tambo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SaleDetailRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long product;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    @Max(value = Integer.MAX_VALUE, message = "La cantidad supera el límite admitido")
    private Long quantity;
}
