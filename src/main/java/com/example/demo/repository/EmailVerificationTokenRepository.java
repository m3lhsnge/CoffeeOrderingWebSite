package com.example.demo.repository;

import com.example.demo.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    //token koduna göre veriyi bulur
    Optional<EmailVerificationToken> findByToken(String token);
    
    //user'a göre token bul
    Optional<EmailVerificationToken> findByUser_Id(Long userId);
}

