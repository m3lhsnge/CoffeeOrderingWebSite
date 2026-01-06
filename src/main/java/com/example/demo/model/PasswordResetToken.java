package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // rastgele üretilen kod (abc-123...)

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user; //sifresini unutan kullanici

    private Date expiryDate; // linkin son kullanma tarihi

    // token'ın geçerliliğini kontrol eden yardımcı metot
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}
