package com.example.Commerce.mappers;

import com.example.Commerce.dtos.AddProductDTO;
import com.example.Commerce.dtos.ProductResponseDTO;
import com.example.Commerce.dtos.UpdateProductDTO;
import com.example.Commerce.entities.ProductEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    ProductResponseDTO toResponseDTO(ProductEntity productEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductEntity toEntity(AddProductDTO addProductDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductDTO updateDTO, @MappingTarget ProductEntity entity);
}
