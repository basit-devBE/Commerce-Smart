package com.example.commerce.dtos.responses;

import com.example.commerce.enums.UserRole;
import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private UserRole role;
}
