package com.example.commerce.interfaces;

import com.example.commerce.entities.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICategoryRepository {
    boolean existsByNameIgnoreCase(String name);

    Optional<CategoryEntity> findByNameIgnoreCase(String name);

    Optional<CategoryEntity> findById(Long id);

    CategoryEntity save(CategoryEntity category);

    void delete(CategoryEntity category);

    Page<CategoryEntity> findAll(Pageable pageable);
}
