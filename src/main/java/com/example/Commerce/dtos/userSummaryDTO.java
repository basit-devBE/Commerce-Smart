package com.example.Commerce.DTOs;

import com.example.Commerce.Enums.UserRole;
import lombok.Data;

@Data
public class userSummaryDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private UserRole role;
}
