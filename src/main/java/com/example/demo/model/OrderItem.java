package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSize selectedSize;

    @Column(nullable = false)
    private Double subTotal;

    // Bir sipariş detayı bir ürüne referans verir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Bir sipariş detayı bir siparişe aittir (Composition - sıkı bağ)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Alt toplamı hesaplayan metot
    public void calculateSubTotal() {
        if (product != null) {
            double sizeMultiplier = getSizeMultiplier(selectedSize);
            this.subTotal = product.getPrice() * quantity * sizeMultiplier;
        }
    }

    // Boyut çarpanı (OOP - Encapsulation prensibi)
    private double getSizeMultiplier(ProductSize size) {
        switch (size) {
            case SMALL: return 1.0;
            case MEDIUM: return 1.2;
            case LARGE: return 1.5;
            default: return 1.0;
        }
    }
}

