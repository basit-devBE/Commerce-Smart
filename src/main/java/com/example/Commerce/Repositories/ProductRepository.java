package com.example.Commerce.Repositories;

import com.example.Commerce.Entities.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsByNameIgnoreCase(@NotBlank(message = "Product name is required") String name);
    Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM ProductEntity p WHERE EXISTS (SELECT i FROM InventoryEntity i WHERE i.product.id = p.id)")
    Page<ProductEntity> findAllWithInventory(Pageable pageable);
    
    @Query("SELECT p FROM ProductEntity p WHERE p.category.id = :categoryId AND EXISTS (SELECT i FROM InventoryEntity i WHERE i.product.id = p.id)")
    Page<ProductEntity> findByCategoryIdWithInventory(Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM ProductEntity p WHERE EXISTS (SELECT i FROM InventoryEntity i WHERE i.product.id = p.id)")
    List<ProductEntity> findAllWithInventory();
}
