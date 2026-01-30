package com.example.Commerce.Services;

import com.example.Commerce.dtos.AddInventoryDTO;
import com.example.Commerce.dtos.InventoryResponseDTO;
import com.example.Commerce.dtos.UpdateInventoryDTO;
import com.example.Commerce.entities.InventoryEntity;
import com.example.Commerce.entities.ProductEntity;
import com.example.Commerce.mappers.InventoryMapper;
import com.example.Commerce.cache.CacheManager;
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

class InventoryServiceTest {

    private InventoryService inventoryService;

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryService(inventoryRepository, productRepository, inventoryMapper, cacheManager);
    }

    @Test
    void addInventory_Success() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);
        dto.setQuantity(100);
        dto.setLocation("Warehouse A");
        
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        
        InventoryEntity savedEntity = new InventoryEntity();
        savedEntity.setId(1L);
        
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProductId(1L)).thenReturn(false);
        when(inventoryRepository.save(any(InventoryEntity.class))).thenReturn(savedEntity);
        when(inventoryMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.addInventory(dto);

        assertNotNull(result);
        verify(inventoryRepository).save(any(InventoryEntity.class));
    }

    @Test
    void addInventory_ProductNotFound() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.addInventory(dto));
    }

    @Test
    void addInventory_AlreadyExists() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);
        
        ProductEntity product = new ProductEntity();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProductId(1L)).thenReturn(true);

        assertThrows(ResourceAlreadyExists.class, () -> inventoryService.addInventory(dto));
    }

    @Test
    void getInventoryById_Success() {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(1L);
        
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(inventoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
    }

    @Test
    void getInventoryByProductId_Success() {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        
        InventoryEntity entity = new InventoryEntity();
        entity.setId(1L);
        
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(entity));
        when(inventoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.getInventoryByProductId(1L);

        assertNotNull(result);
    }

    @Test
    void updateInventory_Success() {
        UpdateInventoryDTO updateDTO = new UpdateInventoryDTO();
        updateDTO.setQuantity(150);
        
        InventoryEntity existingEntity = new InventoryEntity();
        existingEntity.setId(1L);
        
        InventoryEntity updatedEntity = new InventoryEntity();
        updatedEntity.setId(1L);
        
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(inventoryRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(inventoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.updateInventory(1L, updateDTO);

        assertNotNull(result);
    }

    @Test
    void adjustInventoryQuantity_Success() {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(1L);
        entity.setQuantity(100);
        
        InventoryEntity updatedEntity = new InventoryEntity();
        updatedEntity.setId(1L);
        
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(inventoryRepository.save(entity)).thenReturn(updatedEntity);
        when(inventoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.adjustInventoryQuantity(1L, 20);

        assertNotNull(result);
    }

    @Test
    void adjustInventoryQuantity_InsufficientStock() {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(1L);
        entity.setQuantity(10);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> inventoryService.adjustInventoryQuantity(1L, -20));
    }

    @Test
    void deleteInventory_Success() {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> inventoryService.deleteInventory(1L));
        verify(inventoryRepository).delete(entity);
    }
}
