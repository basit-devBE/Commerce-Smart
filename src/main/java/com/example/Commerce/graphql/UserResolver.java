package com.example.Commerce.graphql;

import com.example.Commerce.DTOs.userSummaryDTO;
import com.example.Commerce.interfaces.IUserService;
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
    public List<userSummaryDTO> getAllUsers() {
        return userService.getAllUsersList();
    }

    
}
