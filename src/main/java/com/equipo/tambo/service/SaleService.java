package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleDetailResponse;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.*;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.repository.SaleRepository;
import com.equipo.tambo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SaleDetailService saleDetailService;

    public List<SaleResponse> listSales() {
        return saleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse getById(Long id) {
        return toResponse(getSale(id));
    }

    private SaleEntity getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la venta con ID " + id
                ));
    }

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        UserEntity user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + request.getUser()
                ));

        ClientEntity client = null;

        if(request.getClient() != null) {
            client = clientRepository.findById(request.getClient())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el cliente con ID " + request.getClient()
                    ));
        }

        SaleEntity sale = new SaleEntity();

        sale.setClient(client);
        sale.setUser(user);
        sale.setDate(LocalDateTime.now());

        request.getDetails().forEach(d -> saleDetailService.createSaleDetail(sale, d));

        recalculateTotal(sale);

        return toResponse(saleRepository.save(sale));
    }

    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest request) {
        SaleEntity sale = getSale(id);

        UserEntity user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + request.getUser()
                ));

        ClientEntity client = null;

        if(request.getClient() != null) {
            client = clientRepository.findById(request.getClient())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el cliente con ID " + request.getClient()
                    ));
        }

        sale.setClient(client);
        sale.setUser(user);

        saleDetailService.replaceDetails(sale, request.getDetails());

        recalculateTotal(sale);

        return toResponse(saleRepository.save(sale));
    }

    @Transactional
    public void deleteSale(Long id) {
        SaleEntity sale = getSale(id);
        saleRepository.delete(sale);
    }

    @Transactional
    public SaleResponse updateSaleDetail(Long id, Long detailId, SaleDetailRequest request) {
        SaleEntity sale = getSale(id);

        saleDetailService.updateSaleDetail(sale, detailId, request);

        recalculateTotal(sale);

        return toResponse(sale);
    }

    @Transactional
    public void deleteSaleDetail(Long id, Long detailId) {
        SaleEntity sale = getSale(id);

        saleDetailService.deleteSaleDetail(sale, detailId);

        recalculateTotal(sale);
    }

    private void recalculateTotal(SaleEntity sale) {
        BigDecimal total = sale.getDetails().stream()
                .map(SaleDetailEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setTotal(total);
    }

    private SaleResponse toResponse(SaleEntity sale) {
        List<SaleDetailResponse> detailResponses = sale.getDetails().stream()
                .map(detail -> new SaleDetailResponse(
                        detail.getId(),
                        detail.getProduct(),
                        detail.getQuantity(),
                        detail.getSubtotal()
                )).toList();

        return new SaleResponse(
                sale.getId(),
                sale.getClient(),
                sale.getUser(),
                sale.getDate(),
                sale.getTotal(),
                detailResponses
        );
    }
}