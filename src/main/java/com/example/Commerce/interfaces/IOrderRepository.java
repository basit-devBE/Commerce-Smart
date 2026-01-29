package com.example.Commerce.interfaces;

import com.example.Commerce.Entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IOrderRepository {
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);
    List<OrderEntity> findByUserId(Long userId);
    Page<OrderEntity> findAll(Pageable pageable);
    Optional<OrderEntity> findById(Long id);
    OrderEntity save(OrderEntity order);
    void delete(OrderEntity order);
}
