package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Sale;
import jakarta.validation.Valid;

import java.util.List;

public interface SaleDetailService {
    void createSaleDetail(Sale sale, @Valid SaleDetailRequest request);
    void replaceDetails(Sale sale, List<SaleDetailRequest> requests);
    void updateSaleDetail(Sale sale, Long id, SaleDetailRequest request);
    void deleteSaleDetail(Sale sale, Long id);
}
