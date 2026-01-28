package com.example.Commerce.Repositories;

import com.example.Commerce.Entities.UserEntity;
import com.example.Commerce.Enums.UserRole;
import com.example.Commerce.interfaces.IUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
public class UserRepository implements IUserRepository {
    private final Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    private UserEntity mapRow(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }

    public Optional<UserEntity> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<UserEntity> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public UserEntity save(UserEntity user) {
        try {
            if (user.getId() == null) {
                String sql = "INSERT INTO users (first_name, last_name, email, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getFirstName());
                    ps.setString(2, user.getLastName());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getPassword());
                    ps.setString(5, user.getRole().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            user.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, role = ?, updated_at = NOW() WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, user.getFirstName());
                    ps.setString(2, user.getLastName());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getPassword());
                    ps.setString(5, user.getRole().name());
                    ps.setLong(6, user.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public void delete(UserEntity user) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<UserEntity> findAll(Pageable pageable) {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT * FROM users LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageable.getPageSize());
            ps.setInt(2, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM users";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next()) {
                total = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(users, pageable, total);
    }

    public List<UserEntity> findAll() {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}
