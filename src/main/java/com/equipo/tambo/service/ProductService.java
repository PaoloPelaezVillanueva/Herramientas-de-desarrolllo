package com.equipo.tambo.service;

import com.equipo.tambo.dto.ProductRequest;
import com.equipo.tambo.dto.ProductResponse;
import com.equipo.tambo.entity.Category;
import com.equipo.tambo.entity.Product;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.repository.SaleDetailRepository;
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
    private final CategoryRepository categoryRepository;
    private final SaleDetailRepository saleDetailRepository;

    public List<ProductResponse> listProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        return toResponse(getProduct(id));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el producto con ID " + id
                ));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la categoría con ID " + request.getCategory()
                ));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());
        product.setStock(request.getStock());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProduct(id);

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la categoría con ID " + request.getCategory()
                ));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());
        product.setStock(request.getStock());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProduct(id);

        if(saleDetailRepository.existsByProductId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el producto porque tiene ventas asociadas."
            );
        }

        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCost(),
                product.getStock(),
                product.getActive(),
                product.getCategory()
        );
    }
}