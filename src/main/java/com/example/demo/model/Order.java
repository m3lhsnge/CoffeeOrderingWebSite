package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date createdDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // Ödeme yöntemi

    // Bir sipariş bir kullanıcıya aittir (Aggregation - gevşek bağ)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Bir sipariş bir adrese teslim edilir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address shippingAddress;

    // Bir siparişin birden çok detayı olabilir (Composition - sıkı bağ)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = new Date();
        }
        if (status == null) {
            status = OrderStatus.PREPARING;
        }
        if (totalAmount == null) {
            totalAmount = 0.0;
        }
        calculateTotal();
    }

    // Toplam tutarı hesaplayan metot
    public void calculateTotal() {
        if (items != null && !items.isEmpty()) {
            this.totalAmount = items.stream()
                    .mapToDouble(OrderItem::getSubTotal)
                    .sum();
        } else {
            this.totalAmount = 0.0;
        }
    }
}

