package com.example.Commerce.interfaces;

import com.example.Commerce.DTOs.AddCategoryDTO;
import com.example.Commerce.DTOs.CategoryResponseDTO;
import com.example.Commerce.DTOs.UpdateCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {
    CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO);
    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);
    CategoryResponseDTO getCategoryById(Long id);
    CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);
    void deleteCategory(Long id);
}
