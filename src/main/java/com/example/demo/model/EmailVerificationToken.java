package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // rastgele üretilen kod

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user; // email'i doğrulanacak kullanıcı

    private Date expiryDate; // linkin son kullanma tarihi

    // token'ın geçerliliğini kontrol eden yardımcı metot
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}

