package com.example.Commerce.Mappers;

import com.example.Commerce.DTOs.LoginResponseDTO;
import com.example.Commerce.DTOs.UpdateUserDTO;
import com.example.Commerce.DTOs.UserRegistrationDTO;
import com.example.Commerce.DTOs.userSummaryDTO;
import com.example.Commerce.Entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoginResponseDTO toResponseDTO(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserRegistrationDTO UserRegistrationDTO);

    @Mapping(target = "name", ignore = true)
    userSummaryDTO toSummaryDTO(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateUserDTO updateDTO, @MappingTarget UserEntity entity);
}
