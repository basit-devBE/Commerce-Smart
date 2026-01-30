package com.example.Commerce.mappers;

import com.example.Commerce.dtos.InventoryResponseDTO;
import com.example.Commerce.entities.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "productId", target = "productId")
    @Mapping(target = "productName", ignore = true)
    InventoryResponseDTO toResponseDTO(InventoryEntity inventoryEntity);
}
