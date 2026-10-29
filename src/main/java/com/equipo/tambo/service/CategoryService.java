package com.equipo.tambo.service;

import com.equipo.tambo.dto.CategoryRequest;
import com.equipo.tambo.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> listCategories();
    CategoryResponse getById(Long id);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
