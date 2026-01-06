package com.example.demo.service;

import com.example.demo.model.EmailVerificationToken;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.EmailVerificationTokenRepository;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // EKLENDİ
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder; // BCrypt ile hashleme yapacağız

    public String forgotPassword(String email) {

        Optional<User> userOptional = userRepository.findByEmailAndDeletedFalse(email);
        if (!userOptional.isPresent())
            return "Kullanıcı bulunamadı!";

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
        tokenRepository.save(myToken);

        String link = "http://localhost:8080/auth/reset?token=" + token; // linki düzelttim /auth ekledim
        emailService.sendSimpleEmail(user.getEmail(), "Şifre Sıfırlama", link);
        return "Mail gönderildi";
    }

    // GÜNCELLENEN METOT:
    public String resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (!tokenOptional.isPresent()) {
            return "Geçersiz Token!";
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isExpired()) {
            return "Token süresi dolmuş!";
        }

        User user = resetToken.getUser();
        // Yeni hali (BCrypt ile şifrele):
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "Şifre başarıyla güncellendi!";
    }

    // Kullanıcı kayıt metodu - Şifre BCrypt ile hashlenir, enabled=false
    public User registerUser(User user) {
        // Email zaten kayıtlı mı kontrol et
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Bu email adresi zaten kayıtlı!");
        }

        // Kullanıcının girdiği şifreyi al, hashle ve geri set et
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        // Email doğrulama için enabled=false
        user.setEnabled(false);
        user.setDeleted(false); // Yeni kullanıcılar silinmemiş olarak başlar

        User savedUser = userRepository.save(user);

        // Email doğrulama token'ı oluştur
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 saat
        emailVerificationTokenRepository.save(verificationToken);

        // Email doğrulama linki gönder
        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
        String emailBody = "Merhaba " + savedUser.getName() + ",\n\n" +
                "Hesabınızı doğrulamak için aşağıdaki linke tıklayın:\n" +
                verificationLink + "\n\n" +
                "Bu link 24 saat geçerlidir.\n\n" +
                "İyi günler!";
        emailService.sendSimpleEmail(savedUser.getEmail(), "Email Doğrulama - Kahve Sipariş", emailBody);

        return savedUser;
    }

    // Email doğrulama metodu
    public String verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOptional = emailVerificationTokenRepository.findByToken(token);

        if (!tokenOptional.isPresent()) {
            return "Geçersiz token!";
        }

        EmailVerificationToken verificationToken = tokenOptional.get();

        if (verificationToken.isExpired()) {
            return "Token süresi dolmuş! Lütfen yeni bir kayıt yapın.";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Token'ı sil
        emailVerificationTokenRepository.delete(verificationToken);

        return "Email başarıyla doğrulandı! Artık giriş yapabilirsiniz.";
    }

    // Admin için crud işlemleri -Tüm aktif kullanıcıları getir
    public List<User> getAllUsers() {
        return userRepository.findByDeletedFalse();
    }

    // ID'ye göre aktif kullanıcı getir
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.getDeleted());
    }

    // Kullanıcı ekle (Admin)
    public User addUser(User user) {
        // Şifre hashlenmemişse hashle
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setDeleted(false); // Yeni kullanıcılar silinmemiş olarak başlar
        return userRepository.save(user);
    }

    // Kullanıcı güncelle
    public User updateUser(User user) {
        // Şifre değiştirilmişse hashle
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // Kullanıcı sil (Soft delete)
    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(true); // Soft delete
            userRepository.save(user);
        } else {
            throw new RuntimeException("Silinecek kullanıcı bulunamadı!");
        }
    }

    // kullanıcı kendi profilini güncellemesi için fonksiyonlar

    // Kullanıcı kendi bilgilerini günceller (Email ve Role değiştirilemez)
    public User updateOwnProfile(Long userId, User updatedUser) {
        Optional<User> userOptional = userRepository.findById(userId)
                .filter(user -> !user.getDeleted());

        if (!userOptional.isPresent()) {
            throw new RuntimeException("Kullanıcı bulunamadı!");
        }

        User existingUser = userOptional.get();

        // Sadece name ve surname güncellenebilir
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getSurname() != null) {
            existingUser.setSurname(updatedUser.getSurname());
        }

        return userRepository.save(existingUser);
    }

    // Kullanıcı kendi şifresini değiştirir
    public String changeOwnPassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId)
                .filter(user -> !user.getDeleted());

        if (!userOptional.isPresent()) {
            return "Kullanıcı bulunamadı!";
        }

        User user = userOptional.get();

        // Eski şifreyi kontrol et
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Eski şifre yanlış!";
        }

        // Yeni şifreyi hashle ve kaydet
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Şifre başarıyla güncellendi!";
    }

    // Email'e göre kullanıcı bulma
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email);
    }
}