package com.example.Commerce.interfaces;

import com.example.Commerce.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(Long id);
    UserEntity save(UserEntity user);
    void delete(UserEntity user);
    Page<UserEntity> findAll(Pageable pageable);
    List<UserEntity> findAll();
}
