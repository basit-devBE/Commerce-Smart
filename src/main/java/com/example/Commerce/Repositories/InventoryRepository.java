
package com.example.Commerce.Repositories;

import com.example.Commerce.Entities.InventoryEntity;
import com.example.Commerce.Entities.ProductEntity;

import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Repository
public class InventoryRepository {
    private final Connection connection;

    public InventoryRepository(Connection connection) {
        this.connection = connection;
    }

    private InventoryEntity mapRow(ResultSet rs) throws SQLException {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(rs.getLong("id"));
        ProductEntity product = new ProductEntity();
        product.setId(rs.getLong("product_id"));
        try {
            product.setName(rs.getString("product_name"));
        } catch (SQLException e) {
            // ignore if not present
        }
        inventory.setProduct(product);
        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setLocation(rs.getString("location"));
        return inventory;
    }

    public Optional<InventoryEntity> findByProductId(Long productId) {
        String sql = "SELECT i.*, p.name as product_name FROM inventory i JOIN products p ON i.product_id = p.id WHERE i.product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
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

    public boolean existsByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM inventory WHERE product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
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

    public Optional<InventoryEntity> findById(Long id) {
        String sql = "SELECT i.*, p.name as product_name FROM inventory i JOIN products p ON i.product_id = p.id WHERE i.id = ?";
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

    public Page<InventoryEntity> findAll(Pageable pageable) {
        List<InventoryEntity> inventories = new ArrayList<>();
        String sql = "SELECT i.*, p.name as product_name FROM inventory i JOIN products p ON i.product_id = p.id LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageable.getPageSize());
            ps.setInt(2, (int) pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    inventories.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Get total count
        int total = 0;
        String countSql = "SELECT COUNT(*) FROM inventory";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new PageImpl<>(inventories, pageable, total);
    }

    public InventoryEntity save(InventoryEntity inventory) {
        try {
            if (inventory.getId() == null) {
                String sql = "INSERT INTO inventory (product_id, quantity, location) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, inventory.getProduct().getId());
                    ps.setInt(2, inventory.getQuantity());
                    ps.setString(3, inventory.getLocation());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            inventory.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE inventory SET product_id = ?, quantity = ?, location = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, inventory.getProduct().getId());
                    ps.setInt(2, inventory.getQuantity());
                    ps.setString(3, inventory.getLocation());
                    ps.setLong(4, inventory.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventory;
    }

    public List<InventoryEntity> saveAll(List<InventoryEntity> inventories) {
        for (InventoryEntity inventory : inventories) {
            save(inventory);
        }
        return inventories;
    }

    public void delete(InventoryEntity inventory) {
        String sql = "DELETE FROM inventory WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, inventory.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
