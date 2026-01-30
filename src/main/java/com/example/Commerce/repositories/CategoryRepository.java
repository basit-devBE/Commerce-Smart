package com.example.Commerce.Repositories;

import com.example.Commerce.entities.CategoryEntity;
import com.example.Commerce.interfaces.ICategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
public class CategoryRepository implements ICategoryRepository {
    private final Connection connection;

    public CategoryRepository(Connection connection) {
        this.connection = connection;
    }

    private CategoryEntity mapRow(ResultSet rs) throws SQLException {
        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }

    public boolean existsByNameIgnoreCase(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public Optional<CategoryEntity> findByNameIgnoreCase(String name) {
        String sql = "SELECT * FROM categories WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
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

    public Optional<CategoryEntity> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
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

    public CategoryEntity save(CategoryEntity category) {
        try {
            if (category.getId() == null) {
                String sql = "INSERT INTO categories (name, description, created_at, updated_at) VALUES (?, ?, NOW(), NOW())";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            category.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE categories SET name = ?, description = ?, updated_at = NOW() WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                    ps.setLong(3, category.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return category;
    }

    public void delete(CategoryEntity category) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<CategoryEntity> findAll(Pageable pageable) {
        List<CategoryEntity> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageable.getPageSize());
            ps.setInt(2, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM categories";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next()) {
                total = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(categories, pageable, total);
    }
}
