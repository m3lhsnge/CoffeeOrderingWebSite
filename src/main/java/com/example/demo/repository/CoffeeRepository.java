package com.example.demo.repository;

import com.example.demo.model.Coffee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
    
    // Origin'e göre kahve getir
    List<Coffee> findByOrigin(String origin);
    
    // Roast level'a göre kahve getir
    List<Coffee> findByRoastLevel(String roastLevel);
}

