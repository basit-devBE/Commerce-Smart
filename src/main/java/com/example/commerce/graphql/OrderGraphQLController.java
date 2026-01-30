package com.example.commerce.graphql;

import com.example.commerce.config.GraphQLRequiresRole;
import com.example.commerce.dtos.AddOrderDTO;
import com.example.commerce.dtos.OrderItemDTO;
import com.example.commerce.dtos.OrderResponseDTO;
import com.example.commerce.dtos.UpdateOrderDTO;
import com.example.commerce.enums.OrderStatus;
import com.example.commerce.enums.UserRole;
import com.example.commerce.interfaces.IOrderService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderGraphQLController {
    private final IOrderService orderService;

    public OrderGraphQLController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public List<OrderResponseDTO> allOrders(DataFetchingEnvironment env) {
        return orderService.getAllOrders(Pageable.unpaged()).getContent();
    }

    @QueryMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public OrderResponseDTO orderById(@Argument Long id, DataFetchingEnvironment env) {
        return orderService.getOrderById(id);
    }

    @QueryMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public List<OrderResponseDTO> ordersByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        return orderService.getOrdersByUserId(userId, Pageable.unpaged()).getContent();
    }

    @MutationMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public OrderResponseDTO createOrder(@Argument AddOrderInput input, DataFetchingEnvironment env) {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(input.userId());
        dto.setItems(input.items().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setProductId(item.productId());
                    itemDTO.setQuantity(item.quantity());
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        return orderService.createOrder(dto);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public OrderResponseDTO updateOrderStatus(@Argument Long id, @Argument UpdateOrderInput input, DataFetchingEnvironment env) {
        UpdateOrderDTO dto = new UpdateOrderDTO();
        dto.setStatus(input.status());
        return orderService.updateOrderStatus(id, dto);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public boolean deleteOrder(@Argument Long id, DataFetchingEnvironment env) {
        orderService.deleteOrder(id);
        return true;
    }

    public record AddOrderInput(Long userId, List<OrderItemInput> items) {
    }

    public record OrderItemInput(Long productId, Integer quantity) {
    }

    public record UpdateOrderInput(OrderStatus status) {
    }
}
