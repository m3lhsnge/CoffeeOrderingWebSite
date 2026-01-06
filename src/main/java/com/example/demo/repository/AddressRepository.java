package com.example.demo.repository;

import com.example.demo.model.Address;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Bir kullanıcının tüm adreslerini getir
    List<Address> findByUser(User user);
}

