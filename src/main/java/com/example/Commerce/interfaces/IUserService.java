package com.example.Commerce.interfaces;

import com.example.Commerce.DTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IUserService {
    LoginResponseDTO addUser(UserRegistrationDTO userDTO);
    LoginResponseDTO loginUser(LoginDTO loginDTO);
    userSummaryDTO findUserById(Long id);
    userSummaryDTO updateUser(Long id, UpdateUserDTO userDTO);
    Page<userSummaryDTO> getAllUsers(Pageable pageable);
    List<userSummaryDTO> getAllUsersList();
    void deleteUser(Long id);
}
