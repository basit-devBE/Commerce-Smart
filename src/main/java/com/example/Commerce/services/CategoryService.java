package com.example.Commerce.Services;

import com.example.Commerce.dtos.AddCategoryDTO;
import com.example.Commerce.dtos.CategoryResponseDTO;
import com.example.Commerce.dtos.UpdateCategoryDTO;
import com.example.Commerce.entities.CategoryEntity;
import com.example.Commerce.entities.ProductEntity;
import com.example.Commerce.mappers.CategoryMapper;
import com.example.Commerce.interfaces.ICategoryRepository;
import com.example.Commerce.interfaces.ICategoryService;
import com.example.Commerce.interfaces.IProductRepository;
import com.example.Commerce.interfaces.IInventoryRepository;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService {
    private final ICategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CacheManager cacheManager;
    private final IProductRepository productRepository;
    private final IInventoryRepository inventoryRepository;

    public CategoryService(ICategoryRepository categoryRepository, CategoryMapper categoryMapper, CacheManager cacheManager, IProductRepository productRepository, IInventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.cacheManager = cacheManager;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO) {
        if (categoryRepository.existsByNameIgnoreCase(addCategoryDTO.getName())) {
            throw new ResourceAlreadyExists("Category with name '" + addCategoryDTO.getName() + "' already exists");
        }
        CategoryEntity categoryEntity = categoryMapper.toEntity(addCategoryDTO);
        CategoryEntity savedCategory = categoryRepository.save(categoryEntity);
        return categoryMapper.toResponseDTO(savedCategory);
    }

    public Page<CategoryResponseDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(category -> {
            CategoryResponseDTO dto = categoryMapper.toResponseDTO(category);
            cacheManager.get("category:" + category.getId(), () -> category);
            return dto;
        });
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponseDTO(category);
    }

    public CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        CategoryEntity existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (updateCategoryDTO.getName() != null &&
                !existingCategory.getName().equalsIgnoreCase(updateCategoryDTO.getName()) &&
                categoryRepository.existsByNameIgnoreCase(updateCategoryDTO.getName())) {
            throw new ResourceAlreadyExists("Category with name '" + updateCategoryDTO.getName() + "' already exists");
        }

        categoryMapper.updateEntity(updateCategoryDTO, existingCategory);
        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        
        cacheManager.invalidate("category:" + id);
        
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Get all products in this category
        List<ProductEntity> products = productRepository.findByCategoryId(id);
        
        // Delete inventory and products
        for (ProductEntity product : products) {
            // Delete inventory for this product
            inventoryRepository.findByProductId(product.getId())
                    .ifPresent(inventory -> {
                        inventoryRepository.delete(inventory);
                        cacheManager.invalidate("inventory:" + inventory.getId());
                        cacheManager.invalidate("inventory:product:" + product.getId());
                        cacheManager.invalidate("inventory:quantity:" + product.getId());
                    });
            
            // Delete the product
            productRepository.delete(product);
            cacheManager.invalidate("product:" + product.getId());
        }
        
        // Finally delete the category
        categoryRepository.delete(category);
        cacheManager.invalidate("category:" + id);
    }
}
