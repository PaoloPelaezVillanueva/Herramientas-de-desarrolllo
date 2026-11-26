package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.CategoryRequest;
import com.equipo.tambo.dto.CategoryResponse;
import com.equipo.tambo.entity.Category;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.ProductRepository;
import com.equipo.tambo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getById(Long id) {
        return toResponse(getCategory(id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la categoría con ID " + id
                ));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByCategory(request.getCategory())) {
            throw duplicateCategory();
        }
        Category category = new Category();
        category.setCategory(request.getCategory());

        return saveCategory(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        if (categoryRepository.existsByCategoryAndIdNot(request.getCategory(), id)) {
            throw duplicateCategory();
        }
        category.setCategory(request.getCategory());

        return saveCategory(category);
    }

    private CategoryResponse saveCategory(Category category) {
        try {
            return toResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la categoría ya existe.", ex);
        }
    }

    private ResponseStatusException duplicateCategory() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la categoría ya existe.");
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        String message = "No se puede eliminar la categoría porque tiene productos asociados.";
        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message, ex);
        }
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCategory()
        );
    }
}
