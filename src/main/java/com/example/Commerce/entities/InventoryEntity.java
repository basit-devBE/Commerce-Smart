package com.example.Commerce.entities;

import lombok.*;

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
