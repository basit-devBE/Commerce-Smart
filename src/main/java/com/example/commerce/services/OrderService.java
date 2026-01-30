package com.example.commerce.services;

import com.example.commerce.cache.CacheManager;
import com.example.commerce.dtos.requests.AddOrderDTO;
import com.example.commerce.dtos.requests.OrderItemDTO;
import com.example.commerce.dtos.requests.UpdateOrderDTO;
import com.example.commerce.dtos.responses.OrderItemResponseDTO;
import com.example.commerce.dtos.responses.OrderResponseDTO;
import com.example.commerce.entities.InventoryEntity;
import com.example.commerce.entities.OrderEntity;
import com.example.commerce.entities.OrderItemsEntity;
import com.example.commerce.entities.ProductEntity;
import com.example.commerce.enums.OrderStatus;
import com.example.commerce.errorhandlers.ConstraintViolationException;
import com.example.commerce.errorhandlers.ResourceNotFoundException;
import com.example.commerce.interfaces.*;
import com.example.commerce.mappers.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {
    private final IOrderRepository orderRepository;
    private final IOrderItemsRepository orderItemsRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;
    private final IInventoryRepository inventoryRepository;
    private final OrderMapper orderMapper;
    private final CacheManager cacheManager;

    public OrderService(IOrderRepository orderRepository,
                        IOrderItemsRepository orderItemsRepository,
                        IProductRepository productRepository,
                        IUserRepository userRepository,
                        IInventoryRepository inventoryRepository,
                        OrderMapper orderMapper,
                        CacheManager cacheManager) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderMapper = orderMapper;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public OrderResponseDTO createOrder(AddOrderDTO addOrderDTO) {
        // Validate user exists
        userRepository.findById(addOrderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + addOrderDTO.getUserId()));

        // Calculate total amount, validate products and check inventory
        double totalAmount = 0.0;
        List<OrderItemsEntity> orderItems = new ArrayList<>();
        List<InventoryEntity> inventoriesToUpdate = new ArrayList<>();

        for (OrderItemDTO itemDTO : addOrderDTO.getItems()) {
            ProductEntity product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));

            if (!product.isAvailable()) {
                throw new IllegalArgumentException("Product '" + product.getName() + "' is not available");
            }

            // Check inventory
            InventoryEntity inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product '" + product.getName() + "' is out of stock"));

            if (inventory.getQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Product '" + product.getName() + "' is out of stock");
            }

            // Reduce inventory quantity
            inventory.setQuantity(inventory.getQuantity() - itemDTO.getQuantity());
            inventoriesToUpdate.add(inventory);

            // Invalidate product and inventory caches
            cacheManager.invalidate("product:" + product.getId());
            cacheManager.invalidate("inventory:product:" + product.getId());
            cacheManager.invalidate("inventory:quantity:" + product.getId());

            double itemTotal = product.getPrice() * itemDTO.getQuantity();
            totalAmount += itemTotal;

            OrderItemsEntity orderItem = new OrderItemsEntity();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // Update all inventories
        inventoryRepository.saveAll(inventoriesToUpdate);

        // Create and save order
        OrderEntity order = new OrderEntity();
        order.setUserId(addOrderDTO.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        OrderEntity savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItemsEntity item : orderItems) {
            item.setOrderId(savedOrder.getId());
        }
        List<OrderItemsEntity> savedItems = orderItemsRepository.saveAll(orderItems);

        // Build response
        return buildOrderResponse(savedOrder, savedItems);
    }

    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(order ->
                cacheManager.get("order:" + order.getId(), () -> {
                    List<OrderItemsEntity> items = orderItemsRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, items);
                })
        );
    }

    public Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return orderRepository.findByUserId(userId, pageable).map(order ->
                cacheManager.get("order:" + order.getId(), () -> {
                    List<OrderItemsEntity> items = orderItemsRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, items);
                })
        );
    }

    public OrderResponseDTO getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        List<OrderItemsEntity> items = orderItemsRepository.findByOrderId(order.getId());
        return buildOrderResponse(order, items);
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, UpdateOrderDTO updateOrderDTO) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        if (updateOrderDTO.getStatus() != null) {
            order.setStatus(updateOrderDTO.getStatus());
        }

        OrderEntity updatedOrder = orderRepository.save(order);

        cacheManager.invalidate("order:" + id);

        List<OrderItemsEntity> items = orderItemsRepository.findByOrderId(updatedOrder.getId());
        return buildOrderResponse(updatedOrder, items);
    }

    @Transactional
    public void deleteOrder(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        try {
            List<OrderItemsEntity> items = orderItemsRepository.findByOrderId(id);
            orderItemsRepository.deleteAll(items);
            orderRepository.delete(order);

            cacheManager.invalidate("order:" + id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete order. It has related dependencies that must be removed first.");
            }
            throw ex;
        }
    }

    private OrderResponseDTO buildOrderResponse(OrderEntity order, List<OrderItemsEntity> items) {
        OrderResponseDTO response = orderMapper.toResponseDTO(order);

        // Populate userName
        userRepository.findById(order.getUserId())
                .ifPresent(user -> response.setUserName(user.getFirstName() + " " + user.getLastName()));

        List<OrderItemResponseDTO> itemResponses = items.stream()
                .map(item -> {
                    OrderItemResponseDTO itemResponse = orderMapper.toOrderItemResponseDTO(item);
                    productRepository.findById(item.getProductId())
                            .ifPresent(product -> itemResponse.setProductName(product.getName()));
                    return itemResponse;
                })
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        return response;
    }
}
