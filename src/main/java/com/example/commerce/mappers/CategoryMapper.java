package com.example.commerce.mappers;

import com.example.commerce.dtos.requests.AddCategoryDTO;
import com.example.commerce.dtos.responses.CategoryResponseDTO;
import com.example.commerce.dtos.requests.UpdateCategoryDTO;
import com.example.commerce.entities.CategoryEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDTO(CategoryEntity categoryEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CategoryEntity toEntity(AddCategoryDTO addCategoryDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCategoryDTO updateDTO, @MappingTarget CategoryEntity entity);
}
