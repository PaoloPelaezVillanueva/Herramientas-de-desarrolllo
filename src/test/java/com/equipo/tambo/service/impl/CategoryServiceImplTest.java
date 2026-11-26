package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Category;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock CategoryRepository categoryRepository;
    @Mock ProductRepository productRepository;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository, productRepository);
    }

    @Test
    void rejectsDeletingCategoryWithProducts() {
        Category category = new Category(1L, "Bebidas");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteCategory(1L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(categoryRepository, never()).delete(category);
    }

    @Test
    void deletesCategoryWithoutProducts() {
        Category category = new Category(1L, "Bebidas");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.deleteCategory(1L);

        verify(productRepository).existsByCategoryId(1L);
        verify(categoryRepository).delete(category);
    }
}
