package com.example.Commerce.Entities;

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
