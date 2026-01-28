package com.example.Commerce.interfaces;

import com.example.Commerce.Entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface IOrderRepository {
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);
    Page<OrderEntity> findAll(Pageable pageable);
    Optional<OrderEntity> findById(Long id);
    OrderEntity save(OrderEntity order);
    void delete(OrderEntity order);
}
