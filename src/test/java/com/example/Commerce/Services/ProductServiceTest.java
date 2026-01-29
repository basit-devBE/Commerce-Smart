package com.example.Commerce.Services;

import com.example.Commerce.DTOs.AddProductDTO;
import com.example.Commerce.DTOs.ProductResponseDTO;
import com.example.Commerce.DTOs.UpdateProductDTO;
import com.example.Commerce.Entities.CategoryEntity;
import com.example.Commerce.Entities.ProductEntity;
import com.example.Commerce.Mappers.ProductMapper;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.interfaces.ICategoryRepository;
import com.example.Commerce.interfaces.IInventoryRepository;
import com.example.Commerce.interfaces.IProductRepository;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductService productService;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository, productMapper, categoryRepository, inventoryRepository, cacheManager);
    }

    @Test
    void addProduct_Success() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");
        dto.setCategoryId(1L);
        dto.setSku("LAP001");
        dto.setPrice(999.99);
        
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("Electronics");
        
        ProductEntity entity = new ProductEntity();
        ProductEntity savedEntity = new ProductEntity();
        savedEntity.setId(1L);
        savedEntity.setCategoryId(1L);
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(dto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(savedEntity);
        when(productMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.addProduct(dto);

        assertNotNull(result);
        verify(productRepository).save(entity);
    }

    @Test
    void addProduct_AlreadyExists() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(true);

        assertThrows(ResourceAlreadyExists.class, () -> productService.addProduct(dto));
    }

    @Test
    void addProduct_CategoryNotFound() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");
        dto.setCategoryId(1L);

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(dto));
    }

    @Test
    void getProductById_Success() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Electronics");
        
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setCategoryId(1L);
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productMapper.toResponseDTO(entity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.getProductById(1L);

        assertNotNull(result);
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void updateProduct_Success() {
        UpdateProductDTO updateDTO = new UpdateProductDTO();
        updateDTO.setName("Updated");
        
        CategoryEntity category = new CategoryEntity();
        category.setName("Electronics");
        
        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setId(1L);
        existingEntity.setName("Old");
        existingEntity.setCategoryId(1L);
        
        ProductEntity updatedEntity = new ProductEntity();
        updatedEntity.setId(1L);
        updatedEntity.setCategoryId(1L);
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.existsByNameIgnoreCase("Updated")).thenReturn(false);
        when(productRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(productMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);

        assertNotNull(result);
        verify(productMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteProduct_Success() {
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).delete(entity);
    }
}
