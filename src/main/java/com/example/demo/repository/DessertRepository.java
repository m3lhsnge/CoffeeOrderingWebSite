package com.example.demo.repository;

import com.example.demo.model.Dessert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DessertRepository extends JpaRepository<Dessert, Long> {
    
    // Glutensiz tatlıları getir
    List<Dessert> findByIsGlutenFreeTrue();
    
    // Kalori aralığına göre getir
    List<Dessert> findByCaloriesBetween(int minCalories, int maxCalories);

    // Aktif tatlıları getir
    List<Dessert> findByIsActiveTrue();
}

