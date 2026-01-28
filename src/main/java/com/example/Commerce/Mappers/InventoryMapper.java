package com.example.Commerce.Mappers;

import com.example.Commerce.DTOs.InventoryResponseDTO;
import com.example.Commerce.Entities.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "productId", target = "productId")
    @Mapping(target = "productName", ignore = true)
    InventoryResponseDTO toResponseDTO(InventoryEntity inventoryEntity);
}
