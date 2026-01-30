package com.example.Commerce.interfaces;

import com.example.Commerce.dtos.AddInventoryDTO;
import com.example.Commerce.dtos.InventoryResponseDTO;
import com.example.Commerce.dtos.UpdateInventoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IInventoryService {
    InventoryResponseDTO addInventory(AddInventoryDTO addInventoryDTO);

    Page<InventoryResponseDTO> getAllInventories(Pageable pageable);

    InventoryResponseDTO getInventoryById(Long id);

    InventoryResponseDTO getInventoryByProductId(Long productId);

    InventoryResponseDTO updateInventory(Long id, UpdateInventoryDTO updateInventoryDTO);

    InventoryResponseDTO adjustInventoryQuantity(Long id, Integer quantityChange);

    void deleteInventory(Long id);
}
