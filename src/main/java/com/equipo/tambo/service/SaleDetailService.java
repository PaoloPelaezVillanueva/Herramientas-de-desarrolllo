package com.equipo.tambo.service;

import com.equipo.tambo.dto.SaleDetailRequest;
import com.equipo.tambo.entity.SaleDetailEntity;
import com.equipo.tambo.repository.SaleDetailRepository;
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

    public List<com.equipo.tambo.dto.SaleDetailResponse> listar() {
        return saleDetailRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public com.equipo.tambo.dto.SaleDetailResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerDetalleVenta(id));
    }

    @Transactional
    public com.equipo.tambo.dto.SaleDetailResponse crear(@Valid SaleDetailRequest request) {

        SaleDetailEntity detalleVenta = new SaleDetailEntity();

        copiarDatos(request, detalleVenta);

        return convertirAResponse(
                saleDetailRepository.save(detalleVenta)
        );
    }

    @Transactional
    public com.equipo.tambo.dto.SaleDetailResponse actualizar(
            Long id,
            com.equipo.tambo.dto.SaleDetailRequest request
    ) {

        SaleDetailEntity detalleVenta = obtenerDetalleVenta(id);

        copiarDatos(request, detalleVenta);

        return convertirAResponse(
                saleDetailRepository.save(detalleVenta)
        );
    }

    @Transactional
    public void eliminar(Long id) {

        SaleDetailEntity saleDetail =
                obtenerDetalleVenta(id);

        saleDetailRepository.delete(saleDetail);
    }

    private SaleDetailEntity obtenerDetalleVenta(Long id) {

        return saleDetailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el detalle de venta con ID " + id
                ));
    }

    private void copiarDatos(
            com.equipo.tambo.dto.SaleDetailRequest request,
            SaleDetailEntity detalleVenta
    ) {

        detalleVenta.setIdVenta(request.getIdVenta());
        detalleVenta.setIdProducto(request.getIdProducto());
        detalleVenta.setCantidad(request.getCantidad());

        // El subtotal se calcula posteriormente
    }

    private com.equipo.tambo.dto.SaleDetailResponse convertirAResponse(
            SaleDetailEntity detalleVenta
    ) {

        return new com.equipo.tambo.dto.SaleDetailResponse(
                detalleVenta.getId(),
                detalleVenta.getIdVenta(),
                detalleVenta.getIdProducto(),
                detalleVenta.getCantidad(),
                detalleVenta.getSubtotal()
        );
    }
}