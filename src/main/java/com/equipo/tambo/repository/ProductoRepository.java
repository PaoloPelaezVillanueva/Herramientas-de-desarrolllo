package com.equipo.tambo.repository;

import com.equipo.tambo.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<ProductoEntity, Long> {
}