package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailResponse;
import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.ClientEntity;
import com.equipo.tambo.entity.RoleEntity;
import com.equipo.tambo.entity.SaleEntity;
import com.equipo.tambo.entity.UserEntity;
import com.equipo.tambo.repository.ClientRepository;
import com.equipo.tambo.repository.SaleRepository;
import com.equipo.tambo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        ClientEntity client = clientRepository.findById(request.getClient())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el cliente con ID " + request.getClient()
                ));

        SaleEntity sale = new SaleEntity();

        sale.setClient(client);
        sale.setUser(user);
        sale.setDate(LocalDateTime.now());

        return toResponse(saleRepository.save(sale));
    }

    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest request) {
        UserEntity user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + request.getUser()
                ));

        ClientEntity client = clientRepository.findById(request.getClient())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el cliente con ID " + request.getClient()
                ));

        SaleEntity sale = getSale(id);
        sale.setClient(client);
        sale.setUser(user);
        /* TODO: Actualización de fecha? */

        return toResponse(saleRepository.save(sale));
    }

    @Transactional
    public void deleteSale(Long id) {
        SaleEntity sale = getSale(id);
        saleRepository.delete(sale);
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