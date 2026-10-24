package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;

import java.util.List;

public interface SaleService {
    List<SaleResponse> listSales();
    SaleResponse getById(Long id);
    SaleResponse createSale(SaleRequest request);
    SaleResponse updateSale(Long id, SaleRequest request);
    void deleteSale(Long id);
    SaleResponse updateSaleDetail(Long id, Long detailId, SaleDetailRequest request);
    void deleteSaleDetail(Long id, Long detailId);

}
