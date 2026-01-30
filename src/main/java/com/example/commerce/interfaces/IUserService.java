package com.example.commerce.interfaces;

import com.example.commerce.dtos.requests.LoginDTO;
import com.example.commerce.dtos.requests.UpdateUserDTO;
import com.example.commerce.dtos.requests.UserRegistrationDTO;
import com.example.commerce.dtos.responses.LoginResponseDTO;
import com.example.commerce.dtos.responses.UserSummaryDTO;
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
