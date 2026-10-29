package com.equipo.tambo.repository;

import com.equipo.tambo.entity.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {
    boolean existsByProductId(Long productId);
}