package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleRequest;
import com.equipo.tambo.dto.SaleResponse;
import com.equipo.tambo.entity.SaleEntity;
import com.equipo.tambo.repository.SaleRepository;
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

    public List<SaleResponse> listar() {
        return saleRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerVenta(id));
    }

    @Transactional
    public SaleResponse crear(SaleRequest request) {
        SaleEntity venta = new SaleEntity();
        copiarDatos(request, venta);
        venta.setDate(LocalDateTime.now());

        return convertirAResponse(saleRepository.save(venta));
    }

    @Transactional
    public SaleResponse actualizar(Long id, SaleRequest request) {
        SaleEntity venta = obtenerVenta(id);
        copiarDatos(request, venta);

        return convertirAResponse(saleRepository.save(venta));
    }

    @Transactional
    public void eliminar(Long id) {
        SaleEntity venta = obtenerVenta(id);
        saleRepository.delete(venta);
    }

    private SaleEntity obtenerVenta(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la venta con ID " + id
                ));
    }

    private void copiarDatos(
            SaleRequest request,
            SaleEntity venta
    ) {
        venta.setClienteId(request.getClienteId());
        venta.setUsuarioId(request.getUsuarioId());
    }

    private SaleResponse convertirAResponse(SaleEntity venta) {
        return new SaleResponse(
                venta.getId(),
                venta.getClienteId(),
                venta.getUsuarioId(),
                venta.getDate()
        );
    }
}