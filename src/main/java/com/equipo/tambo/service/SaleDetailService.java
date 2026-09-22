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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleDetailService {
    private final SaleDetailRepository saleDetailRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public List<SaleDetailResponse> listSalesDetail() {
        return saleDetailRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SaleDetailResponse getById(Long id) {
        return toResponse(getSaleDetail(id));
    }

    private SaleDetailEntity getSaleDetail(Long id) {
        return saleDetailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el detalle de venta con ID " + id
                ));
    }

    @Transactional
    public SaleDetailResponse createSaleDetail(@Valid SaleDetailRequest request) {
        SaleEntity sale = saleRepository.findById(request.getSale())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la venta con ID " + request.getSale()
                ));

        ProductEntity product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + request.getProduct()
                ));

        SaleDetailEntity saleDetail = new SaleDetailEntity();

        saleDetail.setSale(sale);
        saleDetail.setProduct(product);
        saleDetail.setQuantity(request.getQuantity());

        return toResponse(saleDetailRepository.save(saleDetail));
    }

    @Transactional
    public SaleDetailResponse updateSaleDetail(Long id, SaleDetailRequest request) {
        SaleEntity sale = saleRepository.findById(request.getSale())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la venta con ID " + request.getSale()
                ));

        ProductEntity product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + request.getProduct()
                ));

        SaleDetailEntity saleDetail = getSaleDetail(id);

        saleDetail.setSale(sale);
        saleDetail.setProduct(product);
        saleDetail.setQuantity(request.getQuantity());

        return toResponse(saleDetailRepository.save(saleDetail));
    }

    @Transactional
    public void deleteSaleDetail(Long id) {
        SaleDetailEntity saleDetail = getSaleDetail(id);

        saleDetailRepository.delete(saleDetail);
    }

    private SaleDetailResponse toResponse(SaleDetailEntity saleDetail) {
        return new SaleDetailResponse(
                saleDetail.getId(),
                saleDetail.getProduct(),
                saleDetail.getQuantity(),
                saleDetail.getSubtotal()
        );
    }
}