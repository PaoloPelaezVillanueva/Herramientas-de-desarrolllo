package com.equipo.tambo.dto;

import com.equipo.tambo.entity.ProductEntity;
import com.equipo.tambo.entity.SaleEntity;
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
    private SaleEntity sale;
    private ProductEntity product;
    private Long quantity;
    private BigDecimal subtotal;


}