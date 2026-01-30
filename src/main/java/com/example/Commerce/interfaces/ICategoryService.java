package com.example.Commerce.interfaces;

import com.example.Commerce.dtos.AddCategoryDTO;
import com.example.Commerce.dtos.CategoryResponseDTO;
import com.example.Commerce.dtos.UpdateCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {
    CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO);
    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);
    CategoryResponseDTO getCategoryById(Long id);
    CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);
    void deleteCategory(Long id);
}
