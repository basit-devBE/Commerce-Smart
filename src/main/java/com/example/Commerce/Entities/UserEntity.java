package com.example.Commerce.Entities;

import com.example.Commerce.Enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole role;
}
