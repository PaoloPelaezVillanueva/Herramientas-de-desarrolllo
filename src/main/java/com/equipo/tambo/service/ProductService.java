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

    private final ProductRepository productRepository;

    public List<ProductResponse> listProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        return toResponse(getProduct(id));
    }

    private ProductEntity getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id
                ));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        ProductEntity product = new ProductEntity();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductEntity product = getProduct(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = getProduct(id);
        productRepository.delete(product);
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCost(),
                product.getStock(),
                product.getActive()
        );
    }
}