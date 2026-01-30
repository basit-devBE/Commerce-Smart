package com.example.commerce.services;

import com.example.commerce.cache.CacheManager;
import com.example.commerce.dtos.*;
import com.example.commerce.entities.OrderEntity;
import com.example.commerce.entities.UserEntity;
import com.example.commerce.errorhandlers.ResourceAlreadyExists;
import com.example.commerce.errorhandlers.ResourceNotFoundException;
import com.example.commerce.interfaces.IOrderItemsRepository;
import com.example.commerce.interfaces.IOrderRepository;
import com.example.commerce.interfaces.IUserRepository;
import com.example.commerce.interfaces.IUserService;
import com.example.commerce.mappers.UserMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final IOrderRepository orderRepository;
    private final IOrderItemsRepository orderItemsRepository;

    public UserService(IUserRepository userRepository, UserMapper userMapper, CacheManager cacheManager, IOrderRepository orderRepository, IOrderItemsRepository orderItemsRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cacheManager = cacheManager;
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    public LoginResponseDTO addUser(UserRegistrationDTO userDTO) {

        Optional<UserEntity> existingUser = userRepository.findByEmail(userDTO.getEmail());
        if (existingUser.isPresent()) {
            throw new ResourceAlreadyExists("Email already exists: " + userDTO.getEmail());
        } else {
            UserEntity userEntity = userMapper.toEntity(userDTO);

            String hashedPassword = BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt());
            userEntity.setPassword(hashedPassword);

            UserEntity savedUser = userRepository.save(userEntity);
            LoginResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
            String randomString = UUID.randomUUID().toString().replace("-", "");
            String token = randomString + "-" + savedUser.getId();
            responseDTO.setToken(token);
            return responseDTO;
        }
    }

    public LoginResponseDTO loginUser(LoginDTO loginDTO) {
        log.info("Attempting login for email: {}", loginDTO.getEmail());
        Optional<UserEntity> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        if (userOpt.isPresent()) {
            UserEntity userEntity = userOpt.get();
            if (BCrypt.checkpw(loginDTO.getPassword(), userEntity.getPassword())) {
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


    public UserSummaryDTO findUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        UserSummaryDTO summary = userMapper.toSummaryDTO(user);
        summary.setName(user.getFirstName() + " " + user.getLastName());
        return summary;
    }

    public UserSummaryDTO updateUser(Long id, @Valid UpdateUserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Handle name splitting if name is provided
        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            String[] nameParts = userDTO.getName().trim().split("\\s+", 2);
            userDTO.setFirstName(nameParts[0]);
            userDTO.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }

        String oldEmail = userEntity.getEmail();
        userMapper.updateEntity(userDTO, userEntity);
        UserEntity updatedUser = userRepository.save(userEntity);

        cacheManager.invalidate("user:" + id);
        cacheManager.invalidate("user:email:" + oldEmail);
        if (userDTO.getEmail() != null && !oldEmail.equals(userDTO.getEmail())) {
            cacheManager.invalidate("user:email:" + userDTO.getEmail());
        }

        UserSummaryDTO summary = userMapper.toSummaryDTO(updatedUser);
        summary.setName(updatedUser.getFirstName() + " " + updatedUser.getLastName());
        return summary;
    }

    public Page<UserSummaryDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user ->
                cacheManager.get("user:" + user.getId(), () -> {
                    UserSummaryDTO summary = userMapper.toSummaryDTO(user);
                    summary.setName(user.getFirstName() + " " + user.getLastName());
                    return summary;
                })
        );
    }

    public void deleteUser(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        String email = userEntity.getEmail();

        // Get all orders for this user
        List<OrderEntity> orders = orderRepository.findByUserId(id);

        // Delete order items and orders
        for (OrderEntity order : orders) {
            orderItemsRepository.deleteAll(orderItemsRepository.findByOrderId(order.getId()));
            orderRepository.delete(order);
            cacheManager.invalidate("order:" + order.getId());
        }

        // Delete the user
        userRepository.delete(userEntity);
        cacheManager.invalidate("user:" + id);
        cacheManager.invalidate("user:email:" + email);
    }
}