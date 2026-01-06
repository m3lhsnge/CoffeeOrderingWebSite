package com.example.demo.service;

import com.example.demo.model.Coffee;
import com.example.demo.model.Dessert;
import com.example.demo.model.Product;
import com.example.demo.repository.CoffeeRepository;
import com.example.demo.repository.DessertRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CoffeeRepository coffeeRepository;

    @Autowired
    private DessertRepository dessertRepository;

    // Tüm ürünleri getir
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Aktif ürünleri getir
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    // ID'ye göre ürün getir
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Kahve ekle
    public Coffee addCoffee(Coffee coffee) {
        return coffeeRepository.save(coffee);
    }

    // Tatlı ekle
    public Dessert addDessert(Dessert dessert) {
        return dessertRepository.save(dessert);
    }

    // Ürün güncelle
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    // Ürün sil (soft delete - isActive false yap)
    public void deleteProduct(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setActive(false);
            productRepository.save(product);
        }
    }

    // Stok güncelle
    public void updateStock(Long productId, int quantity) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.updateStock(quantity);
            productRepository.save(product);
        }
    }

    // Tüm kahveleri getir
    public List<Coffee> getAllCoffees() {
        return coffeeRepository.findAll();
    }

    // Tüm tatlıları getir
    public List<Dessert> getAllDesserts() {
        return dessertRepository.findAll();
    }
}

