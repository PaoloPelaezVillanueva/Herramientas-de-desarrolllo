package com.equipo.tambo.venta.service;

import com.equipo.tambo.venta.dto.VentaRequest;
import com.equipo.tambo.venta.dto.VentaResponse;
import com.equipo.tambo.venta.entity.VentaEntity;
import com.equipo.tambo.venta.repository.VentaRepository;
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
public class VentaService {

    private final VentaRepository ventaRepository;

    public List<VentaResponse> listar() {
        return ventaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public VentaResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerVenta(id));
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        VentaEntity venta = new VentaEntity();
        copiarDatos(request, venta);
        venta.setDate(LocalDateTime.now());

        return convertirAResponse(ventaRepository.save(venta));
    }

    @Transactional
    public VentaResponse actualizar(Long id, VentaRequest request) {
        VentaEntity venta = obtenerVenta(id);
        copiarDatos(request, venta);

        return convertirAResponse(ventaRepository.save(venta));
    }

    @Transactional
    public void eliminar(Long id) {
        VentaEntity venta = obtenerVenta(id);
        ventaRepository.delete(venta);
    }

    private VentaEntity obtenerVenta(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la venta con ID " + id
                ));
    }

    private void copiarDatos(
            VentaRequest request,
            VentaEntity venta
    ) {
        venta.setClienteId(request.getClienteId());
        venta.setUsuarioId(request.getUsuarioId());
    }

    private VentaResponse convertirAResponse(VentaEntity venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getClienteId(),
                venta.getUsuarioId(),
                venta.getDate()
        );
    }
}