package com.example.commerce.mappers;

import com.example.commerce.dtos.OrderItemResponseDTO;
import com.example.commerce.dtos.OrderResponseDTO;
import com.example.commerce.entities.OrderEntity;
import com.example.commerce.entities.OrderItemsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "userId", target = "userId")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "items", ignore = true)
    OrderResponseDTO toResponseDTO(OrderEntity orderEntity);

    @Mapping(source = "productId", target = "productId")
    @Mapping(target = "productName", ignore = true)
    OrderItemResponseDTO toOrderItemResponseDTO(OrderItemsEntity orderItemsEntity);
}
