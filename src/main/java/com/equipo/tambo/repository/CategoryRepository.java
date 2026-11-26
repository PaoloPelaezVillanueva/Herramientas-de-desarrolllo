package com.equipo.tambo.repository;

import com.equipo.tambo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategory(String category);
    boolean existsByCategoryAndIdNot(String category, Long id);
}
