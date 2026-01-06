package com.example.demo.dto;

import com.example.demo.model.ProductSize;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Long productId;
    private int quantity;
    private ProductSize selectedSize;
    private Double subTotal;
}
