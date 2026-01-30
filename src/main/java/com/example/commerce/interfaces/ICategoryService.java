package com.example.commerce.interfaces;

import com.example.commerce.dtos.AddCategoryDTO;
import com.example.commerce.dtos.CategoryResponseDTO;
import com.example.commerce.dtos.UpdateCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {
    CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO);

    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);

    CategoryResponseDTO getCategoryById(Long id);

    CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);

    void deleteCategory(Long id);
}
