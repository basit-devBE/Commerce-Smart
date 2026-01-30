package com.example.Commerce.entities;

import lombok.*;

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
