package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Aktif ürünleri getir
    List<Product> findByIsActiveTrue();
    
    // Stok durumuna göre ürünleri getir
    List<Product> findByStockGreaterThan(int stock);
    
    // Ürün adına göre ara
    List<Product> findByNameContainingIgnoreCase(String name);
}

