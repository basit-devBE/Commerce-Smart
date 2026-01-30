package com.example.Commerce.interfaces;

import com.example.Commerce.entities.InventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IInventoryRepository {
    Optional<InventoryEntity> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
    Optional<InventoryEntity> findById(Long id);
    Page<InventoryEntity> findAll(Pageable pageable);
    InventoryEntity save(InventoryEntity inventory);
    List<InventoryEntity> saveAll(List<InventoryEntity> inventories);
    void delete(InventoryEntity inventory);
}
