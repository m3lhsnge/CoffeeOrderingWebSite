package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String addressType;  // Ev, İş, Diğer
    private String street;       // Sokak
    private String city;         // Şehir
    private String district;     // İlçe
    private String postalCode;   // Posta Kodu
    private String openAddress;  // Detaylı Adres
    
    //bir adres bir kullanıcıya aittir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
}