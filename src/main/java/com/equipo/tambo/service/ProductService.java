package com.equipo.tambo.service;

import com.equipo.tambo.dto.ProductRequest;
import com.equipo.tambo.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> listProducts();
    ProductResponse getById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
