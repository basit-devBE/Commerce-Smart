package com.example.commerce.graphql;

import com.example.commerce.config.GraphQLRequiresRole;
import com.example.commerce.dtos.AddProductDTO;
import com.example.commerce.dtos.ProductResponseDTO;
import com.example.commerce.enums.UserRole;
import com.example.commerce.interfaces.IProductService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductGraphQLController {
    private final IProductService productService;

    public ProductGraphQLController(IProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public List<ProductResponseDTO> allProducts() {
        return productService.getAllProductsList();
    }

    @QueryMapping
    public ProductResponseDTO productById(@Argument Long id) {
        return productService.getProductById(id);
    }

    @MutationMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public ProductResponseDTO addProduct(@Argument AddProductInput input, DataFetchingEnvironment env) {
        AddProductDTO dto = new AddProductDTO();
        dto.setName(input.name());
        dto.setCategoryId(input.categoryId());
        dto.setSku(input.sku());
        dto.setPrice(input.price());
        return productService.addProduct(dto);
    }

    @MutationMapping
    @GraphQLRequiresRole({UserRole.ADMIN, UserRole.SELLER})
    public boolean deleteProduct(@Argument Long id, DataFetchingEnvironment env) {
        productService.deleteProduct(id);
        return true;
    }

    public record AddProductInput(String name, Long categoryId, String sku, Double price) {
    }
}
