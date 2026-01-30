package com.example.Commerce.interfaces;

import com.example.Commerce.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    LoginResponseDTO addUser(UserRegistrationDTO userDTO);
    LoginResponseDTO loginUser(LoginDTO loginDTO);
    UserSummaryDTO findUserById(Long id);
    UserSummaryDTO updateUser(Long id, UpdateUserDTO userDTO);
    Page<UserSummaryDTO> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
}
