package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    //şifreleme algoritması(BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // rol bazlı güvenlik işlemleri admin/müşteri
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Test için korumayı kapattım
            .authorizeHttpRequests(auth -> auth
                // Herkese açık endpoint'ler
                .requestMatchers("/auth/**", "/").permitAll()
                
                // Admin endpoint'leri - Sadece ADMIN rolü erişebilir
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                
                // Müşteri endpoint'leri - Sadece CUSTOMER rolü erişebilir
                .requestMatchers("/customer/**").hasRole("CUSTOMER") 
                
                // Diğer tüm istekler için authentication gerekli
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/perform-login")
                .successHandler(successHandler) // Custom handler kullan
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            );
        
        return http.build();
    }
}
