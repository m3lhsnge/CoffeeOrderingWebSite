package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "desserts")
@PrimaryKeyJoinColumn(name = "product_id")
public class Dessert extends Product {

    private boolean isGlutenFree; // glutensiz mi 
    
    private int calories; // kaç kalori
}