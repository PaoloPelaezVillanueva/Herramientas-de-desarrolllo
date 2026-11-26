package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Sale;

import java.util.List;

public interface SaleDetailService {
    void restoreStock(Sale sale);
    void replaceDetails(Sale sale, List<SaleDetailRequest> requests);
    void updateSaleDetail(Sale sale, Long id, SaleDetailRequest request);
    void deleteSaleDetail(Sale sale, Long id);
}
