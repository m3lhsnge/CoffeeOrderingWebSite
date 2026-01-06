package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Login sayfasını göster
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error, 
                                @RequestParam(required = false) String message,
                                @RequestParam(required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        if (logout != null) {
            model.addAttribute("message", "Başarıyla çıkış yapıldı");
        }
        return "login";
    }

    // Ana sayfa - Login'e yönlendir
    @GetMapping("/")
    public String home() {
        return "redirect:/auth/login";
    }

    // Kayıt işlemi (Form'dan)
    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addAttribute("message", "Kayıt başarılı! Email adresinize gönderilen linke tıklayarak hesabınızı doğrulayın.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Kayıt başarısız: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }
    
    // Email doğrulama
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        String result = userService.verifyEmail(token);
        if (result.contains("başarıyla")) {
            redirectAttributes.addAttribute("message", result);
        } else {
            redirectAttributes.addAttribute("error", result);
        }
        return "redirect:/auth/login";
    }

    // Şifremi unuttum (Form'dan)
    @PostMapping("/forgot")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        String result = userService.forgotPassword(email);
        if (result.contains("Mail gönderildi")) {
            redirectAttributes.addAttribute("message", "Şifre sıfırlama linki e-posta adresinize gönderildi!");
        } else {
            redirectAttributes.addAttribute("error", result);
        }
        return "redirect:/auth/login";
    }

    // Şifre sıfırlama sayfası
    @GetMapping("/reset")
    public String showResetPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    // Şifre sıfırlama işlemi
    @PostMapping("/reset")
    public String resetPassword(@RequestParam String token, 
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        String result = userService.resetPassword(token, password);
        if (result.contains("başarıyla")) {
            redirectAttributes.addAttribute("message", "Şifreniz başarıyla güncellendi! Giriş yapabilirsiniz.");
        } else {
            redirectAttributes.addAttribute("error", result);
        }
        return "redirect:/auth/login";
    }
}