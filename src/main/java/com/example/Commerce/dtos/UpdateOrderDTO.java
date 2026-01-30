package com.example.Commerce.DTOs;

import com.example.Commerce.Enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderDTO {
    private OrderStatus status;
}
