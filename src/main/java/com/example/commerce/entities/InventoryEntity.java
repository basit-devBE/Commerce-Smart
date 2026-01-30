package com.example.commerce.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEntity {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String location;
}
