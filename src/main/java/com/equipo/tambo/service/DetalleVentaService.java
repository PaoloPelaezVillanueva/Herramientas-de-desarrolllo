package com.equipo.tambo.detalleventa.service;

import com.equipo.tambo.detalleventa.dto.DetalleVentaRequest;
import com.equipo.tambo.detalleventa.dto.DetalleVentaResponse;
import com.equipo.tambo.detalleventa.entity.DetalleVentaEntity;
import com.equipo.tambo.detalleventa.repository.DetalleVentaRepository;
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
public class DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;

    public List<DetalleVentaResponse> listar() {
        return detalleVentaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public DetalleVentaResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerDetalleVenta(id));
    }

    @Transactional
    public DetalleVentaResponse crear(DetalleVentaRequest request) {

        DetalleVentaEntity detalleVenta = new DetalleVentaEntity();

        copiarDatos(request, detalleVenta);

        return convertirAResponse(
                detalleVentaRepository.save(detalleVenta)
        );
    }

    @Transactional
    public DetalleVentaResponse actualizar(
            Long id,
            DetalleVentaRequest request
    ) {

        DetalleVentaEntity detalleVenta = obtenerDetalleVenta(id);

        copiarDatos(request, detalleVenta);

        return convertirAResponse(
                detalleVentaRepository.save(detalleVenta)
        );
    }

    @Transactional
    public void eliminar(Long id) {

        DetalleVentaEntity detalleVenta =
                obtenerDetalleVenta(id);

        detalleVentaRepository.delete(detalleVenta);
    }

    private DetalleVentaEntity obtenerDetalleVenta(Long id) {

        return detalleVentaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el detalle de venta con ID " + id
                ));
    }

    private void copiarDatos(
            DetalleVentaRequest request,
            DetalleVentaEntity detalleVenta
    ) {

        detalleVenta.setIdVenta(request.getIdVenta());
        detalleVenta.setIdProducto(request.getIdProducto());
        detalleVenta.setCantidad(request.getCantidad());

        // El subtotal se calcula posteriormente
    }

    private DetalleVentaResponse convertirAResponse(
            DetalleVentaEntity detalleVenta
    ) {

        return new DetalleVentaResponse(
                detalleVenta.getId(),
                detalleVenta.getIdVenta(),
                detalleVenta.getIdProducto(),
                detalleVenta.getCantidad(),
                detalleVenta.getSubtotal()
        );
    }
}