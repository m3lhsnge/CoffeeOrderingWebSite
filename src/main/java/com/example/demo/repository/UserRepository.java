package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // email ile kullanıcı bulma metodu (silinmemiş kullanıcılar için)
    Optional<User> findByEmailAndDeletedFalse(String email);

    // Email ile kullanıcı bulma (tüm kullanıcılar - kayıt kontrolü için)
    Optional<User> findByEmail(String email);

    // Silinmemiş tüm kullanıcıları getir
    List<User> findByDeletedFalse();
}