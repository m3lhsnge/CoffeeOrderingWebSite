package com.example.demo.dto;

import com.example.demo.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private OrderStatus status;
    private Double totalAmount;
    private String message;
}
