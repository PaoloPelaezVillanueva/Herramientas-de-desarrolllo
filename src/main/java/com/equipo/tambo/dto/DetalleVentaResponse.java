package com.equipo.tambo.detalleventa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DetalleVentaResponse {

    private Long id;
    private Long idVenta;
    private Long idProducto;
    private Long cantidad;
    private BigDecimal subtotal;
}