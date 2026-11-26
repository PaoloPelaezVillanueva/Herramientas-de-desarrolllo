package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.entity.SaleDetail;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.service.SaleDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleDetailServiceImpl implements SaleDetailService {
    private final ProductRepository productRepository;

    private SaleDetail getSaleDetail(Sale sale, Long id) {
        return sale.getDetails().stream()
                .filter(detail -> id.equals(detail.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el detalle con ID " + id + " en la venta con ID " + sale.getId()));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id));
    }

    private void adjustStock(Product product, long unitsToDeduct) {
        long remaining = product.getStock() - unitsToDeduct;
        if (remaining < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para '" + product.getName() + "'. Stock disponible: " + product.getStock());
        }
        if (remaining > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El stock resultante supera el límite para '" + product.getName() + "'.");
        }
        product.setStock((int) remaining);
        productRepository.save(product);
    }

    @Override
    public void createSaleDetail(Sale sale, @Valid SaleDetailRequest request) {
        Product product = getProduct(request.getProduct());
        adjustStock(product, request.getQuantity());
        SaleDetail detail = new SaleDetail();
        setDetail(detail, product, request.getQuantity());
        sale.addDetail(detail);
    }

    @Override
    public void restoreStock(Sale sale) {
        for (SaleDetail detail : sale.getDetails()) {
            adjustStock(detail.getProduct(), -detail.getQuantity());
        }
    }

    @Override
    public void replaceDetails(Sale sale, List<SaleDetailRequest> requests) {
        restoreStock(sale);
        sale.getDetails().clear();
        for (SaleDetailRequest request : requests) {
            createSaleDetail(sale, request);
        }
    }

    @Override
    public void updateSaleDetail(Sale sale, Long id, SaleDetailRequest request) {
        SaleDetail detail = getSaleDetail(sale, id);
        Product previous = detail.getProduct();
        Product product = getProduct(request.getProduct());
        if (previous.getId().equals(product.getId())) {
            adjustStock(product, request.getQuantity() - detail.getQuantity());
        } else {
            adjustStock(product, request.getQuantity());
            adjustStock(previous, -detail.getQuantity());
        }
        setDetail(detail, product, request.getQuantity());
    }

    @Override
    public void deleteSaleDetail(Sale sale, Long id) {
        SaleDetail detail = getSaleDetail(sale, id);
        adjustStock(detail.getProduct(), -detail.getQuantity());
        sale.getDetails().remove(detail);
    }

    private void setDetail(SaleDetail detail, Product product, long quantity) {
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setSubtotal(product.getCost().multiply(BigDecimal.valueOf(quantity)));
    }
}
