package com.example.Commerce.Services;

import com.example.Commerce.dtos.*;
import com.example.Commerce.entities.UserEntity;
import com.example.Commerce.mappers.UserMapper;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.interfaces.IUserRepository;
import com.example.Commerce.interfaces.IOrderRepository;
import com.example.Commerce.interfaces.IOrderItemsRepository;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderItemsRepository orderItemsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, userMapper, cacheManager, orderRepository, orderItemsRepository);
    }

    @Test
    void addUser_Success() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        
        UserEntity entity = new UserEntity();
        entity.setPassword("password123");
        
        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(1L);
        
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setId(1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        when(userMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        LoginResponseDTO result = userService.addUser(dto);

        assertNotNull(result);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void addUser_EmailAlreadyExists() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@example.com");
        
        UserEntity existingUser = new UserEntity();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceAlreadyExists.class, () -> userService.addUser(dto));
    }

    @Test
    void loginUser_Success() {
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail(email);
        userEntity.setPassword(hashedPassword);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        LoginResponseDTO expectedResponse = new LoginResponseDTO();
        expectedResponse.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponseDTO(userEntity)).thenReturn(expectedResponse);

        LoginResponseDTO actualResponse = userService.loginUser(loginDTO);

        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getToken());
    }

    @Test
    void loginUser_InvalidPassword() {
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        UserEntity userEntity = new UserEntity();
        userEntity.setPassword(hashedPassword);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword("wrong");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        assertThrows(IllegalArgumentException.class, () -> userService.loginUser(loginDTO));
    }

    @Test
    void loginUser_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.loginUser(loginDTO));
    }

    @Test
    void findUserById_Success() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        
        userSummaryDTO summaryDTO = new userSummaryDTO();
        summaryDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toSummaryDTO(entity)).thenReturn(summaryDTO);

        userSummaryDTO result = userService.findUserById(1L);

        assertNotNull(result);
    }

    @Test
    void findUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    void updateUser_Success() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        
        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(1L);
        
        UserEntity updatedEntity = new UserEntity();
        updatedEntity.setId(1L);
        
        userSummaryDTO summaryDTO = new userSummaryDTO();
        summaryDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(userMapper.toSummaryDTO(updatedEntity)).thenReturn(summaryDTO);

        userSummaryDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        verify(userMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteUser_Success() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(orderRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).delete(entity);
    }
}
