package com.example.Commerce.mappers;

import com.example.Commerce.dtos.LoginResponseDTO;
import com.example.Commerce.dtos.UpdateUserDTO;
import com.example.Commerce.dtos.UserRegistrationDTO;
import com.example.Commerce.dtos.UserSummaryDTO;
import com.example.Commerce.entities.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "token", ignore = true)
    LoginResponseDTO toResponseDTO(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserRegistrationDTO UserRegistrationDTO);

    @Mapping(target = "name", ignore = true)
    UserSummaryDTO toSummaryDTO(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserDTO updateDTO, @MappingTarget UserEntity entity);
}
