package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.SaleDetail;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
import com.equipo.tambo.service.SaleDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleDetailServiceImpl implements SaleDetailService {
    private final SaleDetailRepository saleDetailRepository;
    private final ProductRepository productRepository;

    private SaleDetail getSaleDetail(Long id) {
        return saleDetailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el detalle de venta con ID " + id
                ));
    }

    @Override
    public void createSaleDetail(Sale sale, @Valid SaleDetailRequest request) {
        Product product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + request.getProduct()
                ));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para '" + product.getName() + "'. Stock disponible: " + product.getStock()
            );
        }

        product.setStock(product.getStock() - request.getQuantity().intValue());
        productRepository.save(product);

        SaleDetail saleDetail = new SaleDetail();

        saleDetail.setProduct(product);
        saleDetail.setQuantity(request.getQuantity());

        BigDecimal subtotal = product.getCost().multiply(BigDecimal.valueOf(request.getQuantity()));
        saleDetail.setSubtotal(subtotal);

        sale.addDetail(saleDetail);
    }

    @Override
    public void replaceDetails(Sale sale, List<SaleDetailRequest> requests) {
        sale.getDetails().clear();

        for (SaleDetailRequest request : requests) {
            createSaleDetail(sale, request);
        }
    }

    @Override
    public void updateSaleDetail(Sale sale, Long id, SaleDetailRequest request) {
        SaleDetail saleDetail = getSaleDetail(id);

        if(!saleDetail.getSale().getId().equals(sale.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El detalle con ID " + id + " no pertenece a la venta con ID " + sale.getId()
            );
        }

        Product product = productRepository.findById(request.getProduct())
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

    @Override
    public void deleteSaleDetail(Sale sale, Long id) {
        SaleDetail saleDetail = getSaleDetail(id);

        if(!saleDetail.getSale().getId().equals(sale.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El detalle con ID " + id + " no pertenece a la venta con ID " + sale.getId()
            );
        }

        Product product = saleDetail.getProduct();
        product.setStock(product.getStock() + saleDetail.getQuantity().intValue());
        productRepository.save(product);

        sale.getDetails().remove(saleDetail);
    }

    @Override
    public void validateStock(List<SaleDetailRequest> requests) {
        Map<Long, Long> requestedByProduct = new LinkedHashMap<>();

        for (SaleDetailRequest request : requests) {
            requestedByProduct.merge(request.getProduct(), request.getQuantity(), Long::sum);
        }

        for (Map.Entry<Long, Long> entry : requestedByProduct.entrySet()) {
            Long productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el producto con ID " + productId
                    ));

            long availableStock = product.getStock();

            if (availableStock < requestedQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para '" + product.getName()
                                + "'. Stock disponible: " + availableStock
                                + ". Cantidad solicitada: " + requestedQuantity
                );
            }
        }
    }
}