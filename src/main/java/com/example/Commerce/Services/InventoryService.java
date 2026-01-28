package com.example.Commerce.Services;

import com.example.Commerce.DTOs.AddInventoryDTO;
import com.example.Commerce.DTOs.InventoryResponseDTO;
import com.example.Commerce.DTOs.UpdateInventoryDTO;
import com.example.Commerce.Entities.InventoryEntity;
import com.example.Commerce.Entities.ProductEntity;
import com.example.Commerce.Mappers.InventoryMapper;
import com.example.Commerce.Repositories.InventoryRepository;
import com.example.Commerce.Repositories.ProductRepository;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.errorHandlers.ConstraintViolationException;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;
    private final CacheManager cacheManager;

    public InventoryService(InventoryRepository inventoryRepository, 
                           ProductRepository productRepository,
                           InventoryMapper inventoryMapper,
                           CacheManager cacheManager) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
        this.cacheManager = cacheManager;
    }

    public InventoryResponseDTO addInventory(AddInventoryDTO addInventoryDTO) {
        // Validate product exists
        ProductEntity product = productRepository.findById(addInventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + addInventoryDTO.getProductId()));

        // Check if inventory already exists for this product
        if (inventoryRepository.existsByProductId(addInventoryDTO.getProductId())) {
            throw new ResourceAlreadyExists("Inventory already exists for product ID: " + addInventoryDTO.getProductId());
        }

        InventoryEntity inventoryEntity = new InventoryEntity();
        inventoryEntity.setProduct(product);
        inventoryEntity.setQuantity(addInventoryDTO.getQuantity());
        inventoryEntity.setLocation(addInventoryDTO.getLocation());

        InventoryEntity savedInventory = inventoryRepository.save(inventoryEntity);
        return inventoryMapper.toResponseDTO(savedInventory);
    }

    public Page<InventoryResponseDTO> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
            .map(inventory -> cacheManager.get("inventory:" + inventory.getId(), () -> inventoryMapper.toResponseDTO(inventory)));
    }

    public InventoryResponseDTO getInventoryById(Long id) {
        return cacheManager.get("inventory:" + id, () -> {
            InventoryEntity inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
            return inventoryMapper.toResponseDTO(inventory);
        });
    }

    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        return cacheManager.get("inventory:product:" + productId, () -> {
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
            return inventoryMapper.toResponseDTO(inventory);
        });
    }

    public InventoryResponseDTO updateInventory(Long id, UpdateInventoryDTO updateInventoryDTO) {
        InventoryEntity existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        if (updateInventoryDTO.getQuantity() != null) {
            existingInventory.setQuantity(updateInventoryDTO.getQuantity());
        }
        
        if (updateInventoryDTO.getLocation() != null) {
            existingInventory.setLocation(updateInventoryDTO.getLocation());
        }

        return getInventoryResponseDTO(id, existingInventory);
    }

    private InventoryResponseDTO getInventoryResponseDTO(Long id, InventoryEntity existingInventory) {
        InventoryEntity updatedInventory = inventoryRepository.save(existingInventory);

        cacheManager.invalidate("inventory:" + id);
        cacheManager.invalidate("inventory:product:" + existingInventory.getProduct().getId());
        cacheManager.invalidate("inventory:quantity:" + existingInventory.getProduct().getId());
        cacheManager.invalidate("product:" + existingInventory.getProduct().getId());

        return inventoryMapper.toResponseDTO(updatedInventory);
    }

    public InventoryResponseDTO adjustInventoryQuantity(Long id, Integer quantityChange) {
        InventoryEntity inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        int newQuantity = inventory.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient inventory. Available: " + inventory.getQuantity() + ", Required: " + Math.abs(quantityChange));
        }

        inventory.setQuantity(newQuantity);
        return getInventoryResponseDTO(id, inventory);
    }

    public void deleteInventory(Long id) {
        InventoryEntity inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        Long productId = inventory.getProduct().getId();
        
        try {
            inventoryRepository.delete(inventory);
            cacheManager.invalidate("inventory:" + id);
            cacheManager.invalidate("inventory:product:" + productId);
            cacheManager.invalidate("inventory:quantity:" + productId);
            cacheManager.invalidate("product:" + productId);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
            throw new ConstraintViolationException(
                    "Cannot delete inventory. It has related dependencies that must be removed first.");
            }
            throw ex;
        }
    }
}
