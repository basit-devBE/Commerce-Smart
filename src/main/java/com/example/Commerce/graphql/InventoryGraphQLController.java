package com.example.Commerce.graphql;

import com.example.Commerce.config.GraphQLRequiresRole;
import com.example.Commerce.dtos.AddInventoryDTO;
import com.example.Commerce.dtos.InventoryResponseDTO;
import com.example.Commerce.dtos.UpdateInventoryDTO;
import com.example.Commerce.enums.UserRole;
import com.example.Commerce.interfaces.IInventoryService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class InventoryGraphQLController {
    private final IInventoryService inventoryService;

    public InventoryGraphQLController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @QueryMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public List<InventoryResponseDTO> allInventories(DataFetchingEnvironment env) {
        return inventoryService.getAllInventories(Pageable.unpaged()).getContent();
    }

    @QueryMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public InventoryResponseDTO inventoryById(@Argument Long id, DataFetchingEnvironment env) {
        return inventoryService.getInventoryById(id);
    }

    @QueryMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public InventoryResponseDTO inventoryByProductId(@Argument Long productId, DataFetchingEnvironment env) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @MutationMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public InventoryResponseDTO addInventory(@Argument AddInventoryInput input, DataFetchingEnvironment env) {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(input.productId());
        dto.setQuantity(input.quantity());
        dto.setLocation(input.location());
        return inventoryService.addInventory(dto);
    }

    @MutationMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public InventoryResponseDTO updateInventory(@Argument Long id, @Argument UpdateInventoryInput input, DataFetchingEnvironment env) {
        UpdateInventoryDTO dto = new UpdateInventoryDTO();
        dto.setQuantity(input.quantity());
        dto.setLocation(input.location());
        return inventoryService.updateInventory(id, dto);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public boolean deleteInventory(@Argument Long id, DataFetchingEnvironment env) {
        inventoryService.deleteInventory(id);
        return true;
    }

    public record AddInventoryInput(Long productId, Integer quantity, String location) {}
    public record UpdateInventoryInput(Integer quantity, String location) {}
}
