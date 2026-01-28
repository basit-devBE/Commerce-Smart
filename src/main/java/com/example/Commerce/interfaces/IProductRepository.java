package com.example.Commerce.interfaces;

import com.example.Commerce.Entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IProductRepository {
    boolean existsByNameIgnoreCase(String name);
    Optional<ProductEntity> findById(Long id);
    ProductEntity save(ProductEntity product);
    void delete(ProductEntity product);
    Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable);
    List<ProductEntity> findByCategoryId(Long categoryId);
    Page<ProductEntity> findAllWithInventory(Pageable pageable);
    Page<ProductEntity> findByCategoryIdWithInventory(Long categoryId, Pageable pageable);
    List<ProductEntity> findAllWithInventory();
}
