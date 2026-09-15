package com.equipo.tambo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailResponse {

    private Long id;
    private Long idVenta;
    private Long idProducto;
    private Long cantidad;
    private BigDecimal subtotal;


}