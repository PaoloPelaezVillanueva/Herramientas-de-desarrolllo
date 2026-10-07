package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleDetailResponse;
import com.equipo.tambo.entity.ProductEntity;
import com.equipo.tambo.entity.SaleDetailEntity;
import com.equipo.tambo.entity.SaleEntity;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
import com.equipo.tambo.repository.SaleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleDetailService {
    private final SaleDetailRepository saleDetailRepository;
    private final ProductRepository productRepository;

    private SaleDetailEntity getSaleDetail(Long id) {
        return saleDetailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el detalle de venta con ID " + id
                ));
    }

    public void createSaleDetail(SaleEntity sale, @Valid SaleDetailRequest request) {
        ProductEntity product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + request.getProduct()
                ));

        SaleDetailEntity saleDetail = new SaleDetailEntity();

        saleDetail.setProduct(product);
        saleDetail.setQuantity(request.getQuantity());

        BigDecimal subtotal = product.getCost().multiply(BigDecimal.valueOf(request.getQuantity()));
        saleDetail.setSubtotal(subtotal);

        sale.addDetail(saleDetail);
    }

    public void replaceDetails(SaleEntity sale, List<SaleDetailRequest> requests) {
        sale.getDetails().clear();

        for (SaleDetailRequest request : requests) {
            createSaleDetail(sale, request);
        }
    }

    public void updateSaleDetail(SaleEntity sale, Long id, SaleDetailRequest request) {
        SaleDetailEntity saleDetail = getSaleDetail(id);

        if(!saleDetail.getSale().getId().equals(sale.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El detalle con ID " + id + " no pertenece a la venta con ID " + sale.getId()
            );
        }

        ProductEntity product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + request.getProduct()
                ));

        saleDetail.setProduct(product);
        saleDetail.setQuantity(request.getQuantity());

        BigDecimal subtotal = saleDetail.getProduct()
                .getCost()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        saleDetail.setSubtotal(subtotal);
    }

    public void deleteSaleDetail(SaleEntity sale, Long id) {
        SaleDetailEntity saleDetail = getSaleDetail(id);

        if(!saleDetail.getSale().getId().equals(sale.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El detalle con ID " + id + " no pertenece a la venta con ID " + sale.getId()
            );
        }

        sale.getDetails().remove(saleDetail);
    }
}