package com.equipo.tambo.producto.repository;

import com.equipo.tambo.producto.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<ProductoEntity, Long> {
}