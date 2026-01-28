package com.example.Commerce.Mappers;

import com.example.Commerce.DTOs.OrderItemResponseDTO;
import com.example.Commerce.DTOs.OrderResponseDTO;
import com.example.Commerce.Entities.OrderEntity;
import com.example.Commerce.Entities.OrderItemsEntity;
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
