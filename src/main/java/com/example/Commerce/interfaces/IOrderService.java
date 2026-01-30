package com.example.Commerce.interfaces;

import com.example.Commerce.dtos.AddOrderDTO;
import com.example.Commerce.dtos.OrderResponseDTO;
import com.example.Commerce.dtos.UpdateOrderDTO;
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
