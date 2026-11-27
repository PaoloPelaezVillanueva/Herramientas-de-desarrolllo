package com.equipo.tambo.service.impl;

import com.equipo.tambo.entity.Category;
import com.equipo.tambo.repository.CategoryRepository;
import com.equipo.tambo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplDeleteTest {
    @Mock CategoryRepository categoryRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks CategoryServiceImpl service;

    @Test
    void deleteRemovesEntityWhenNoRelatedRecords() {
        Category entity = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);

        service.deleteCategory(1L);

        verify(categoryRepository).delete(entity);
    }

    @Test
    void deleteThrowsConflictWhenRelatedRecordsExist() {
        Category entity = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteCategory(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteCategory(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(categoryRepository, never()).delete(any());
    }
}
