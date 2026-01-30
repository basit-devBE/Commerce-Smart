package com.example.Commerce.mappers;

import com.example.Commerce.dtos.AddCategoryDTO;
import com.example.Commerce.dtos.CategoryResponseDTO;
import com.example.Commerce.dtos.UpdateCategoryDTO;
import com.example.Commerce.entities.CategoryEntity;
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
