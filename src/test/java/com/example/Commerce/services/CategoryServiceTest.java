package com.example.Commerce.services;

import com.example.Commerce.dtos.AddCategoryDTO;
import com.example.Commerce.dtos.CategoryResponseDTO;
import com.example.Commerce.dtos.UpdateCategoryDTO;
import com.example.Commerce.entities.CategoryEntity;
import com.example.Commerce.mappers.CategoryMapper;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.interfaces.ICategoryRepository;
import com.example.Commerce.interfaces.IProductRepository;
import com.example.Commerce.interfaces.IInventoryRepository;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryService categoryService;

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IInventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoryRepository, categoryMapper, cacheManager, productRepository, inventoryRepository);
    }

    @Test
    void addCategory_Success() {
        AddCategoryDTO dto = new AddCategoryDTO();
        dto.setName("Electronics");
        dto.setDescription("Electronic items");
        
        CategoryEntity entity = new CategoryEntity();
        CategoryEntity savedEntity = new CategoryEntity();
        savedEntity.setId(1L);
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(savedEntity);
        when(categoryMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.addCategory(dto);

        assertNotNull(result);
        verify(categoryRepository).save(entity);
    }

    @Test
    void addCategory_AlreadyExists() {
        AddCategoryDTO dto = new AddCategoryDTO();
        dto.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThrows(ResourceAlreadyExists.class, () -> categoryService.addCategory(dto));
    }

    @Test
    void getCategoryById_Success() {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.getCategoryById(1L);

        assertNotNull(result);
    }

    @Test
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void updateCategory_Success() {
        UpdateCategoryDTO updateDTO = new UpdateCategoryDTO();
        updateDTO.setName("Updated");
        
        CategoryEntity existingEntity = new CategoryEntity();
        existingEntity.setId(1L);
        existingEntity.setName("Old");
        
        CategoryEntity updatedEntity = new CategoryEntity();
        updatedEntity.setId(1L);
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(categoryRepository.existsByNameIgnoreCase("Updated")).thenReturn(false);
        when(categoryRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(categoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.updateCategory(1L, updateDTO);

        assertNotNull(result);
        verify(categoryMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteCategory_Success() {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).delete(entity);
    }
}
