package com.example.demo.repository;

import com.example.demo.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<PasswordResetToken, Long> {
    //token koduna göre veriyi bulur
    Optional<PasswordResetToken> findByToken(String token);
}