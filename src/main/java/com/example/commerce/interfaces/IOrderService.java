package com.example.commerce.interfaces;

import com.example.commerce.dtos.AddOrderDTO;
import com.example.commerce.dtos.OrderResponseDTO;
import com.example.commerce.dtos.UpdateOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    OrderResponseDTO createOrder(AddOrderDTO addOrderDTO);

    Page<OrderResponseDTO> getAllOrders(Pageable pageable);

    Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable);

    OrderResponseDTO getOrderById(Long id);

    OrderResponseDTO updateOrderStatus(Long id, UpdateOrderDTO updateOrderDTO);

    void deleteOrder(Long id);
}
