package com.example.commerce.graphql;

import com.example.commerce.config.GraphQLRequiresRole;
import com.example.commerce.dtos.AddCategoryDTO;
import com.example.commerce.dtos.CategoryResponseDTO;
import com.example.commerce.dtos.UpdateCategoryDTO;
import com.example.commerce.enums.UserRole;
import com.example.commerce.interfaces.ICategoryService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CategoryGraphQLController {
    private final ICategoryService categoryService;

    public CategoryGraphQLController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @QueryMapping
    public List<CategoryResponseDTO> allCategories() {
        return categoryService.getAllCategories(Pageable.unpaged()).getContent();
    }

    @QueryMapping
    public CategoryResponseDTO categoryById(@Argument Long id) {
        return categoryService.getCategoryById(id);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public CategoryResponseDTO addCategory(@Argument AddCategoryInput input, DataFetchingEnvironment env) {
        AddCategoryDTO dto = new AddCategoryDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        return categoryService.addCategory(dto);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public CategoryResponseDTO updateCategory(@Argument Long id, @Argument UpdateCategoryInput input, DataFetchingEnvironment env) {
        UpdateCategoryDTO dto = new UpdateCategoryDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        return categoryService.updateCategory(id, dto);
    }

    @MutationMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public boolean deleteCategory(@Argument Long id, DataFetchingEnvironment env) {
        categoryService.deleteCategory(id);
        return true;
    }

    public record AddCategoryInput(String name, String description) {
    }

    public record UpdateCategoryInput(String name, String description) {
    }
}
