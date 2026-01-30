package com.example.Commerce.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsEntity {
    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
}
