package com.example.commerce.services;

import com.example.commerce.dtos.*;
import com.example.commerce.entities.*;
import com.example.commerce.enums.OrderStatus;
import com.example.commerce.mappers.OrderMapper;
import com.example.commerce.cache.CacheManager;
import com.example.commerce.interfaces.*;
import com.example.commerce.errorHandlers.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderService orderService;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderItemsRepository orderItemsRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, orderItemsRepository, productRepository, 
                                       userRepository, inventoryRepository, orderMapper, cacheManager);
    }

    @Test
    void createOrder_Success() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);
        
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        dto.setItems(List.of(itemDTO));
        
        UserEntity user = new UserEntity();
        user.setId(1L);
        
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setPrice(999.99);
        product.setAvailable(true);
        
        InventoryEntity inventory = new InventoryEntity();
        inventory.setQuantity(10);
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        
        OrderItemsEntity savedItem = new OrderItemsEntity();
        savedItem.setId(1L);
        
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(orderItemsRepository.saveAll(anyList())).thenReturn(List.of(savedItem));
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);
        when(orderMapper.toOrderItemResponseDTO(any())).thenReturn(new OrderItemResponseDTO());

        OrderResponseDTO result = orderService.createOrder(dto);

        assertNotNull(result);
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_UserNotFound() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_ProductNotAvailable() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);
        
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        dto.setItems(List.of(itemDTO));
        
        UserEntity user = new UserEntity();
        
        ProductEntity product = new ProductEntity();
        product.setAvailable(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_InsufficientStock() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);
        
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(20);
        dto.setItems(List.of(itemDTO));
        
        UserEntity user = new UserEntity();
        
        ProductEntity product = new ProductEntity();
        product.setAvailable(true);
        
        InventoryEntity inventory = new InventoryEntity();
        inventory.setQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void getOrderById_Success() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemsRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(orderMapper.toResponseDTO(order)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
    }

    @Test
    void updateOrderStatus_Success() {
        UpdateOrderDTO updateDTO = new UpdateOrderDTO();
        updateDTO.setStatus(OrderStatus.SHIPPED);
        
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        
        OrderEntity updatedOrder = new OrderEntity();
        updatedOrder.setId(1L);
        
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);
        when(orderItemsRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(orderMapper.toResponseDTO(updatedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, updateDTO);

        assertNotNull(result);
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_Success() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemsRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
        verify(orderRepository).delete(order);
    }
}
