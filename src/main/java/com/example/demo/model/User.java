package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN veya CUSTOMER

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean enabled = false; // Email doğrulama durumu

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean deleted = false; // Soft delete için

    //bir kullanıcının birden çok adresi olabilir
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
}