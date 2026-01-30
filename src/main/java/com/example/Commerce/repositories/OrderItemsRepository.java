package com.example.Commerce.Repositories;

import com.example.Commerce.entities.OrderItemsEntity;
import com.example.Commerce.interfaces.IOrderItemsRepository;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
public class OrderItemsRepository implements IOrderItemsRepository {
    private final Connection connection;

    public OrderItemsRepository(Connection connection) {
        this.connection = connection;
    }

    private OrderItemsEntity mapRow(ResultSet rs) throws SQLException {
        OrderItemsEntity item = new OrderItemsEntity();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setTotalPrice(rs.getDouble("total_price"));
        return item;
    }

    public List<OrderItemsEntity> findByOrderId(Long orderId) {
        List<OrderItemsEntity> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    public List<OrderItemsEntity> saveAll(List<OrderItemsEntity> items) {
        for (OrderItemsEntity item : items) {
            save(item);
        }
        return items;
    }

    public OrderItemsEntity save(OrderItemsEntity item) {
        try {
            if (item.getId() == null) {
                String sql = "INSERT INTO order_items (order_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, item.getOrderId());
                    ps.setLong(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getTotalPrice());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            item.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE order_items SET order_id = ?, product_id = ?, quantity = ?, total_price = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, item.getOrderId());
                    ps.setLong(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getTotalPrice());
                    ps.setLong(5, item.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    public void deleteAll(List<OrderItemsEntity> items) {
        for (OrderItemsEntity item : items) {
            delete(item);
        }
    }

    public void delete(OrderItemsEntity item) {
        String sql = "DELETE FROM order_items WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
