package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;

import java.util.List;
import java.util.UUID;

public interface SaleService {
    List<SaleResponse> listSales();
    SaleResponse getById(Long id);
    SaleResponse createSale(SaleRequest request, UUID requestKey);
    SaleResponse updateSale(Long id, SaleRequest request);
    void deleteSale(Long id);
    SaleResponse updateSaleDetail(Long id, Long detailId, SaleDetailRequest request);
    void deleteSaleDetail(Long id, Long detailId);

}
