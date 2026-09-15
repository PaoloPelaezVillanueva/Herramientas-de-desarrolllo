package com.equipo.tambo.detalleventa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale", nullable = false)
    private Long idVenta;

    @Column(name = "product", nullable = false)
    private Long idProducto;

    @Column(name = "quantity", nullable = false)
    private Long cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}