package com.equipo.tambo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String nombre;

    @Column(name = "lastname", nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(length = 150)
    private String email;

    @Column(name = "address", length = 255)
    private String direccion;
}