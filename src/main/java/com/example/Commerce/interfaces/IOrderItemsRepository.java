package com.example.Commerce.interfaces;

import com.example.Commerce.entities.OrderItemsEntity;

import java.util.List;

public interface IOrderItemsRepository {
    List<OrderItemsEntity> findByOrderId(Long orderId);

    List<OrderItemsEntity> saveAll(List<OrderItemsEntity> items);

    OrderItemsEntity save(OrderItemsEntity item);

    void deleteAll(List<OrderItemsEntity> items);

    void delete(OrderItemsEntity item);
}
