package com.example.Commerce.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class JdbcConnectionConfig {
    @Bean
    public Connection jdbcConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/ECommerceDB";
        String username = "basit";
        String password = "bece2018";
        return DriverManager.getConnection(url, username, password);
    }
}
