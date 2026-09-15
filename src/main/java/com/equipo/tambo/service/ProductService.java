package com.equipo.tambo.service;

import com.equipo.tambo.dto.ProductRequest;
import com.equipo.tambo.dto.ProductResponse;
import com.equipo.tambo.entity.ProductEntity;
import com.equipo.tambo.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productoRepository;

    public List<ProductResponse> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerProducto(id));
    }

    @Transactional
    public ProductResponse crear(ProductRequest request) {
        ProductEntity producto = new ProductEntity();
        copiarDatos(request, producto);

        return convertirAResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductResponse actualizar(Long id, ProductRequest request) {
        ProductEntity producto = obtenerProducto(id);
        copiarDatos(request, producto);

        return convertirAResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        ProductEntity producto = obtenerProducto(id);
        productoRepository.delete(producto);
    }

    private ProductEntity obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id
                ));
    }

    private void copiarDatos(
            ProductRequest request,
            ProductEntity producto
    ) {
        producto.setName(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCost(request.getPrecio());
    }

    private ProductResponse convertirAResponse(ProductEntity producto) {
        return new ProductResponse(
                producto.getId(),
                producto.getName(),
                producto.getDescripcion(),
                producto.getCost(),
                producto.getStock(),
                producto.getActivo()
        );
    }
}