package com.equipo.tambo.venta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client")
    private Long clienteId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "user", nullable = false)
    private Long usuarioId;
}