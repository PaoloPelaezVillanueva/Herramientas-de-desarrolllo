package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.dto.SaleDetailResponse;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.*;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.repository.SaleRepository;
import com.equipo.tambo.repository.SaleSubmissionRepository;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.service.SaleDetailService;
import com.equipo.tambo.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleServiceImpl implements SaleService {
    private static final BigDecimal MAX_TOTAL = new BigDecimal("99999999.99");

    private final SaleRepository saleRepository;
    private final SaleSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SaleDetailService saleDetailService;

    @Override
    public List<SaleResponse> listSales() {
        return saleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SaleResponse getById(Long id) {
        return toResponse(getSale(id));
    }

    private Sale getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> saleNotFound(id));
    }

    private Sale getSaleForUpdate(Long id) {
        return saleRepository.findByIdForUpdate(id).orElseThrow(() -> saleNotFound(id));
    }

    private ResponseStatusException saleNotFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la venta con ID " + id);
    }

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request, UUID requestKey) {
        // Lock the seller row first so retries with the same key are checked serially.
        var seller = requestKey == null ? userRepository.findById(request.getUser())
                : userRepository.findByIdForUpdate(request.getUser());
        User user = seller
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + request.getUser()
                ));

        String submissionId = null;
        String fingerprint = null;
        if (requestKey != null) {
            submissionId = user.getId() + ":" + requestKey;
            fingerprint = fingerprint(request);
            SaleSubmission previous = submissionRepository.findById(submissionId).orElse(null);
            if (previous != null) {
                return replaySale(previous, fingerprint);
            }
        }

        Client client = null;

        if(request.getClient() != null) {
            client = clientRepository.findById(request.getClient())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el cliente con ID " + request.getClient()
                    ));
        }

        Sale sale = new Sale();

        sale.setClient(client);
        sale.setUser(user);
        sale.setDate(LocalDateTime.now());

        saleDetailService.replaceDetails(sale, request.getDetails());

        recalculateTotal(sale);

        Sale saved = saleRepository.save(sale);
        if (submissionId != null) {
            submissionRepository.save(new SaleSubmission(submissionId, fingerprint, saved.getId()));
        }
        return toResponse(saved);
    }

    private SaleResponse replaySale(SaleSubmission previous, String fingerprint) {
        if (!previous.getFingerprint().equals(fingerprint)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La clave de compra ya se usó con datos distintos.");
        }
        return saleRepository.findById(previous.getSaleId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE,
                        "La venta de esta compra ya fue eliminada."));
    }

    private String fingerprint(SaleRequest request) {
        StringBuilder contents = new StringBuilder().append(request.getClient());
        for (SaleDetailRequest detail : request.getDetails()) {
            contents.append(':').append(detail.getProduct()).append(':').append(detail.getQuantity());
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(contents.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    @Override
    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest request) {
        Sale sale = getSaleForUpdate(id);

        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + request.getUser()
                ));

        Client client = null;

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

    @Override
    @Transactional
    public void deleteSale(Long id) {
        Sale sale = getSaleForUpdate(id);
        saleDetailService.restoreStock(sale);
        saleRepository.delete(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateSaleDetail(Long id, Long detailId, SaleDetailRequest request) {
        Sale sale = getSaleForUpdate(id);

        saleDetailService.updateSaleDetail(sale, detailId, request);

        recalculateTotal(sale);

        return toResponse(sale);
    }

    @Override
    @Transactional
    public void deleteSaleDetail(Long id, Long detailId) {
        Sale sale = getSaleForUpdate(id);

        saleDetailService.deleteSaleDetail(sale, detailId);

        recalculateTotal(sale);
    }

    private void recalculateTotal(Sale sale) {
        BigDecimal total = sale.getDetails().stream()
                .map(SaleDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El total supera el importe máximo admitido para una venta.");
        }
        sale.setTotal(total);
    }

    private SaleResponse toResponse(Sale sale) {
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
