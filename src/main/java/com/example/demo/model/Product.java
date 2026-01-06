package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED) // KRİTİK NOKTA: Miras stratejisi
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // ürün ad

    @Column(nullable = false)
    private Double price; // min fiyat

    private String description; 
    
    private String imagePath; //resim icin path(YOL)
    private int stock; //basit stok takibi için

    private boolean isActive = true; // menüden kaldırmak silmek yerine pasife çekeriz(soft delete işlemi)

    // Stok güncelleme metodu 
    public void updateStock(int quantity) {
        this.stock += quantity;
        if (this.stock < 0) {
            this.stock = 0; // Stok negatif olamaz
        }
    }
}