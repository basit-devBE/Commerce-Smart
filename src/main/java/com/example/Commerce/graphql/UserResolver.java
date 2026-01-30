package com.example.Commerce.graphql;

import com.example.Commerce.config.GraphQLRequiresRole;
import com.example.Commerce.dtos.*;
import com.example.Commerce.enums.UserRole;
import com.example.Commerce.interfaces.IUserService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserResolver {
    private final IUserService userService;

    public UserResolver(IUserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public List<UserSummaryDTO> getAllUsers(DataFetchingEnvironment env) {
        return userService.getAllUsers(Pageable.unpaged()).getContent();
    }

    @MutationMapping
    public AuthResponse login(@Argument LoginInput input) {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(input.email());
        dto.setPassword(input.password());
        LoginResponseDTO response = userService.loginUser(dto);
        return new AuthResponse(response.getToken(), response.getId(), response.getEmail(), 
                response.getFirstName(), response.getLastName(), response.getRole());
    }

    @MutationMapping
    public AuthResponse register(@Argument RegisterInput input) {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFirstName(input.firstName());
        dto.setLastName(input.lastName());
        dto.setEmail(input.email());
        dto.setPassword(input.password());
        dto.setRole(input.role());
        LoginResponseDTO response = userService.addUser(dto);
        return new AuthResponse(response.getToken(), response.getId(), response.getEmail(), 
                response.getFirstName(), response.getLastName(), response.getRole());
    }

    public record LoginInput(String email, String password) {}
    public record RegisterInput(String firstName, String lastName, String email, String password, UserRole role) {}
    public record AuthResponse(String token, Long id, String email, String firstName, String lastName, String role) {}
}
