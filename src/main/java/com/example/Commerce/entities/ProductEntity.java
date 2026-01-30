package com.example.Commerce.entities;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductEntity {
    private Long id;
    private String name;
    private Long categoryId;
    private String sku;
    private Double price;
    private boolean isAvailable = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
