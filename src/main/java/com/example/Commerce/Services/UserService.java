package com.example.Commerce.Services;

import com.example.Commerce.DTOs.UserRegistrationDTO;
import com.example.Commerce.DTOs.UserResponseDTO;
import com.example.Commerce.Entities.UserEntity;
import com.example.Commerce.Mappers.UserMapper;
import com.example.Commerce.Repositories.UserRepository;
import com.example.Commerce.errorHandlers.EmailAlreadyExists;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDTO addUser(UserRegistrationDTO userDTO){
        Optional<UserEntity> existingUser = userRepository.findByEmail(userDTO.getEmail());
        if(existingUser.isPresent()){
            throw new EmailAlreadyExists("Email already exists: " + userDTO.getEmail());
        } else {
            UserEntity userEntity = userMapper.toEntity(userDTO);
            UserEntity savedUser = userRepository.save(userEntity);
            return userMapper.toResponseDTO(savedUser);
        }
    }
}