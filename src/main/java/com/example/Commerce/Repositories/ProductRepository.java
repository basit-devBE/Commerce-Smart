package com.example.Commerce.Repositories;

import com.example.Commerce.Entities.CategoryEntity;
import com.example.Commerce.Entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
public class ProductRepository {
    private final Connection connection;

    public ProductRepository(Connection connection) {
        this.connection = connection;
    }

    private ProductEntity mapRow(ResultSet rs) throws SQLException {
        ProductEntity product = new ProductEntity();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setSku(rs.getString("sku"));
        product.setPrice(rs.getDouble("price"));
        product.setAvailable(rs.getBoolean("is_available"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getLong("category_id"));
        try {
            category.setName(rs.getString("category_name"));
        } catch (SQLException e) {
            // ignore if not present
        }
        product.setCategory(category);
        return product;
    }

    public boolean existsByNameIgnoreCase(String name) {
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(?)";
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

    public Optional<ProductEntity> findById(Long id) {
        String sql = "SELECT p.*, c.name as category_name FROM products p JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
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

    public ProductEntity save(ProductEntity product) {
        try {
            if (product.getId() == null) {
                String sql = "INSERT INTO products (name, category_id, sku, price, is_available, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, product.getName());
                    ps.setLong(2, product.getCategory().getId());
                    ps.setString(3, product.getSku());
                    ps.setDouble(4, product.getPrice());
                    ps.setBoolean(5, product.isAvailable());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            product.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE products SET name = ?, category_id = ?, sku = ?, price = ?, is_available = ?, updated_at = NOW() WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, product.getName());
                    ps.setLong(2, product.getCategory().getId());
                    ps.setString(3, product.getSku());
                    ps.setDouble(4, product.getPrice());
                    ps.setBoolean(5, product.isAvailable());
                    ps.setLong(6, product.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return product;
    }

    public void delete(ProductEntity product) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ps.setInt(2, pageable.getPageSize());
            ps.setInt(3, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(countSql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(products, pageable, total);
    }

    public Page<ProductEntity> findAllWithInventory(Pageable pageable) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p JOIN categories c ON p.category_id = c.id WHERE EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id) LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageable.getPageSize());
            ps.setInt(2, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM products p WHERE EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id)";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next()) {
                total = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(products, pageable, total);
    }

    public Page<ProductEntity> findByCategoryIdWithInventory(Long categoryId, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM products p WHERE p.category_id = ? AND EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id)";
        long total = 0;
        try (PreparedStatement ps = connection.prepareStatement(countSql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? AND EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id) LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ps.setInt(2, pageable.getPageSize());
            ps.setInt(3, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(products, pageable, total);
    }

    public List<ProductEntity> findAllWithInventory() {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p JOIN categories c ON p.category_id = c.id WHERE EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id)";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return products;
    }
}
