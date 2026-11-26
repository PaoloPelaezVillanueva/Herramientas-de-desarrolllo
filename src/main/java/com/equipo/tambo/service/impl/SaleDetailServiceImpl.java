package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.entity.Sale;
import com.equipo.tambo.entity.SaleDetail;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.service.SaleDetailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleDetailServiceImpl implements SaleDetailService {
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    private SaleDetail getSaleDetail(Sale sale, Long id) {
        return sale.getDetails().stream()
                .filter(detail -> id.equals(detail.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el detalle con ID " + id + " en la venta con ID " + sale.getId()));
    }

    private Product lockProduct(Long id, Product existing) {
        if (existing != null) {
            // The sale may have loaded this product before the lock was acquired.
            entityManager.refresh(existing, LockModeType.PESSIMISTIC_WRITE);
            return existing;
        }
        return productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id));
    }

    private void requireActive(Product product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede vender el producto inactivo '" + product.getName() + "'.");
        }
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
    }

    private Map<Long, Product> applyStockChanges(Map<Long, Long> changes, Map<Long, Product> existing) {
        Map<Long, Product> products = new HashMap<>();
        // Every sale acquires product locks in ID order, regardless of the line order.
        new TreeMap<>(changes).forEach((id, units) -> {
            Product product = lockProduct(id, existing.get(id));
            if (units > 0) {
                requireActive(product);
            }
            if (units != 0) {
                adjustStock(product, units);
            }
            products.put(id, product);
        });
        return products;
    }

    @Override
    public void restoreStock(Sale sale) {
        Map<Long, Long> changes = new HashMap<>();
        Map<Long, Product> existing = new HashMap<>();
        collectExistingStock(sale, changes, existing);
        applyStockChanges(changes, existing);
    }

    @Override
    public void replaceDetails(Sale sale, List<SaleDetailRequest> requests) {
        Map<Long, Long> changes = new HashMap<>();
        Map<Long, Product> existing = new HashMap<>();
        collectExistingStock(sale, changes, existing);
        Map<Long, Deque<SaleDetail>> previousDetails = new HashMap<>();
        for (SaleDetail detail : sale.getDetails()) {
            previousDetails.computeIfAbsent(detail.getProduct().getId(), id -> new ArrayDeque<>()).add(detail);
        }
        for (SaleDetailRequest request : requests) {
            changes.merge(request.getProduct(), request.getQuantity(), Long::sum);
        }

        Map<Long, Product> products = applyStockChanges(changes, existing);
        sale.getDetails().clear();
        for (SaleDetailRequest request : requests) {
            Deque<SaleDetail> prior = previousDetails.get(request.getProduct());
            SaleDetail previous = prior != null ? prior.poll() : null;
            Product product = products.get(request.getProduct());
            SaleDetail detail = new SaleDetail();
            setDetail(detail, product, request.getQuantity(), previous);
            sale.addDetail(detail);
        }
    }

    private void collectExistingStock(Sale sale, Map<Long, Long> changes, Map<Long, Product> existing) {
        for (SaleDetail detail : sale.getDetails()) {
            Product product = detail.getProduct();
            existing.put(product.getId(), product);
            changes.merge(product.getId(), -detail.getQuantity(), Long::sum);
        }
    }

    @Override
    public void updateSaleDetail(Sale sale, Long id, SaleDetailRequest request) {
        SaleDetail detail = getSaleDetail(sale, id);
        Product previous = detail.getProduct();
        Map<Long, Long> changes = new HashMap<>();
        changes.put(previous.getId(), -detail.getQuantity());
        changes.merge(request.getProduct(), request.getQuantity(), Long::sum);
        Map<Long, Product> existing = new HashMap<>();
        for (SaleDetail line : sale.getDetails()) {
            Product product = line.getProduct();
            existing.put(product.getId(), product);
        }
        Product product = applyStockChanges(changes, existing).get(request.getProduct());
        setDetail(detail, product, request.getQuantity(), detail);
    }

    @Override
    public void deleteSaleDetail(Sale sale, Long id) {
        SaleDetail detail = getSaleDetail(sale, id);
        Product product = detail.getProduct();
        applyStockChanges(Map.of(product.getId(), -detail.getQuantity()), Map.of(product.getId(), product));
        sale.getDetails().remove(detail);
    }

    private void setDetail(SaleDetail detail, Product product, long quantity, SaleDetail previous) {
        BigDecimal units = BigDecimal.valueOf(quantity);
        BigDecimal subtotal;
        if (previous != null && previous.getProduct().getId().equals(product.getId())) {
            subtotal = previous.getSubtotal().multiply(units)
                    .divide(BigDecimal.valueOf(previous.getQuantity()), 2, RoundingMode.HALF_UP);
        } else {
            subtotal = product.getCost().multiply(units);
        }
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setSubtotal(subtotal);
    }
}
