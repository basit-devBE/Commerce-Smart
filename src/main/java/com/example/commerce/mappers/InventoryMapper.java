package com.example.commerce.mappers;

import com.example.commerce.dtos.InventoryResponseDTO;
import com.example.commerce.entities.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "productId", target = "productId")
    @Mapping(target = "productName", ignore = true)
    InventoryResponseDTO toResponseDTO(InventoryEntity inventoryEntity);
}
