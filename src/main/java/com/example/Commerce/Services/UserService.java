package com.example.Commerce.Services;

import com.example.Commerce.DTOs.*;
import com.example.Commerce.Entities.UserEntity;
import com.example.Commerce.Mappers.UserMapper;
import com.example.Commerce.interfaces.IUserRepository;
import com.example.Commerce.interfaces.IUserService;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    public UserService(IUserRepository userRepository, UserMapper userMapper, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cacheManager = cacheManager;
    }

    public LoginResponseDTO addUser(UserRegistrationDTO userDTO){

        Optional<UserEntity> existingUser = userRepository.findByEmail(userDTO.getEmail());
        if(existingUser.isPresent()){
            throw new ResourceAlreadyExists("Email already exists: " + userDTO.getEmail());
        } else {
            UserEntity userEntity = userMapper.toEntity(userDTO);
            
            String hashedPassword = BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt());
            userEntity.setPassword(hashedPassword);

            UserEntity savedUser = userRepository.save(userEntity);
            return userMapper.toResponseDTO(savedUser);
        }
    }

    public LoginResponseDTO loginUser(LoginDTO loginDTO){
        log.info("Attempting login for email: {}", loginDTO.getEmail());
        Optional<UserEntity> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        if(userOpt.isPresent()){
            UserEntity userEntity = userOpt.get();
            if(BCrypt.checkpw(loginDTO.getPassword(), userEntity.getPassword())){
                String randomString = UUID.randomUUID().toString().replace("-", "");
                String token = randomString + "-" + userEntity.getId();
                LoginResponseDTO responseDTO = userMapper.toResponseDTO(userEntity);
                responseDTO.setToken(token);
                return responseDTO;
            } else {
                throw new IllegalArgumentException("Invalid password");
            }
        } else {
            throw new ResourceNotFoundException("User not found with email: " + loginDTO.getEmail());
        }
    }

    public userSummaryDTO findUserByEmail(String email){
        return cacheManager.get("user:email:" + email, () -> {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            userSummaryDTO summary = userMapper.toSummaryDTO(user);
            summary.setName(user.getFirstName() + " " + user.getLastName());
            return summary;
        });
    }

    public userSummaryDTO findUserById(Long id){
        return cacheManager.get("user:" + id, () -> {
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
            userSummaryDTO summary = userMapper.toSummaryDTO(user);
            summary.setName(user.getFirstName() + " " + user.getLastName());
            return summary;
        });
    }
    public userSummaryDTO updateUser(Long id, @Valid UpdateUserDTO userDTO){
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        String oldEmail = userEntity.getEmail();
        userMapper.updateEntity(userDTO, userEntity);
        UserEntity updatedUser = userRepository.save(userEntity);
        
        cacheManager.invalidate("user:" + id);
        cacheManager.invalidate("user:email:" + oldEmail);
        if (userDTO.getEmail() != null && !oldEmail.equals(userDTO.getEmail())) {
            cacheManager.invalidate("user:email:" + userDTO.getEmail());
        }
        
        userSummaryDTO summary = userMapper.toSummaryDTO(updatedUser);
        summary.setName(updatedUser.getFirstName() + " " + updatedUser.getLastName());
        return summary;
    }

    public Page<userSummaryDTO> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable).map(user -> 
            cacheManager.get("user:" + user.getId(), () -> {
                userSummaryDTO summary = userMapper.toSummaryDTO(user);
                summary.setName(user.getFirstName() + " " + user.getLastName());
                return summary;
            })
        );
    }

    public List<userSummaryDTO> getAllUsersList() {
        return userRepository.findAll().stream()
            .map(user -> cacheManager.get("user:" + user.getId(), () -> {
                userSummaryDTO summary = userMapper.toSummaryDTO(user);
                summary.setName(user.getFirstName() + " " + user.getLastName());
                return summary;
            }))
            .toList();
    }

    public void deleteUser(Long id){
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        String email = userEntity.getEmail();
        userRepository.delete(userEntity);
        cacheManager.invalidate("user:" + id);
        cacheManager.invalidate("user:email:" + email);
    }
}