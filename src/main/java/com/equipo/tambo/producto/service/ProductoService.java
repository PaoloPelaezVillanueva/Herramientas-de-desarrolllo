package com.equipo.tambo.producto.service;

import com.equipo.tambo.producto.dto.ProductoRequest;
import com.equipo.tambo.producto.dto.ProductoResponse;
import com.equipo.tambo.producto.entity.ProductoEntity;
import com.equipo.tambo.producto.repository.ProductoRepository;
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
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<ProductoResponse> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public ProductoResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerProducto(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        ProductoEntity producto = new ProductoEntity();
        copiarDatos(request, producto);

        return convertirAResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        ProductoEntity producto = obtenerProducto(id);
        copiarDatos(request, producto);

        return convertirAResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoEntity producto = obtenerProducto(id);
        productoRepository.delete(producto);
    }

    private ProductoEntity obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id
                ));
    }

    private void copiarDatos(
            ProductoRequest request,
            ProductoEntity producto
    ) {
        producto.setName(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCost(request.getPrecio());
    }

    private ProductoResponse convertirAResponse(ProductoEntity producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getName(),
                producto.getDescripcion(),
                producto.getCost(),
                producto.getStock(),
                producto.getActivo()
        );
    }
}