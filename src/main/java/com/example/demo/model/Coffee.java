package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // üst sınıftan eşitlik kontrollerini de yap
@Entity
@Table(name = "coffees")
@PrimaryKeyJoinColumn(name = "product_id") // product tablosuna bu ID ile bağlan
public class Coffee extends Product {

    private String origin; //ait oldugu yer
    
    private String roastLevel; // Örn:Medium, Dark ...
}