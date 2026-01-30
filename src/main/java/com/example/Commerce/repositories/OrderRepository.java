package com.example.Commerce.Repositories;

import com.example.Commerce.entities.OrderEntity;
import com.example.Commerce.enums.OrderStatus;
import com.example.Commerce.interfaces.IOrderRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {
    private final Connection connection;

    public OrderRepository(Connection connection) {
        this.connection = connection;
    }

    private OrderEntity mapRow(ResultSet rs) throws SQLException {
        OrderEntity order = new OrderEntity();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return order;
    }

    public Page<OrderEntity> findByUserId(Long userId, Pageable pageable) {
        List<OrderEntity> orders = new ArrayList<>();
        boolean paged = pageable != null && pageable.isPaged();
        String sql;
        if (paged) {
            sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        } else {
            sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            if (paged && pageable != null) {
                ps.setInt(2, pageable.getPageSize());
                ps.setInt(3, (int) pageable.getOffset());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int total = orders.size();
        return new PageImpl<>(orders, pageable == null ? Pageable.unpaged() : pageable, total);
    }

    public List<OrderEntity> findByUserId(Long userId) {
        List<OrderEntity> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orders;
    }

    public Page<OrderEntity> findAll(Pageable pageable) {
        List<OrderEntity> orders = new ArrayList<>();
        boolean paged = pageable != null && pageable.isPaged();
        String sql;
        if (paged) {
            sql = "SELECT o.*, u.email as user_email FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        } else {
            sql = "SELECT o.*, u.email as user_email FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (paged && pageable != null) {
                ps.setInt(1, pageable.getPageSize());
                ps.setInt(2, (int) pageable.getOffset());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int total = orders.size();
        return new PageImpl<>(orders, pageable == null ? Pageable.unpaged() : pageable, total);
    }

    public Optional<OrderEntity> findById(Long id) {
        String sql = "SELECT o.*, u.email as user_email FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
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

    public OrderEntity save(OrderEntity order) {
        try {
            if (order.getId() == null) {
                String sql = "INSERT INTO orders (user_id, total_amount, status, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, order.getUserId());
                    ps.setDouble(2, order.getTotalAmount());
                    ps.setString(3, order.getStatus().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            order.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE orders SET user_id = ?, total_amount = ?, status = ?, updated_at = NOW() WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, order.getUserId());
                    ps.setDouble(2, order.getTotalAmount());
                    ps.setString(3, order.getStatus().name());
                    ps.setLong(4, order.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    public void delete(OrderEntity order) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, order.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
