package com.example.demo.dto;

import com.example.demo.model.PaymentMethod;
import com.example.demo.model.OrderStatus;
import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private UserDTO user;
    private AddressDTO shippingAddress;
    private PaymentMethod paymentMethod;
    private List<OrderItemDTO> items;
    private OrderStatus status;

    @Data
    public static class UserDTO {
        private Long id;
    }

    @Data
    public static class AddressDTO {
        private Long id;
    }
}
