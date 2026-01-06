package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderResponseDTO;
import com.example.demo.model.*;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.demo.repository.AddressRepository addressRepository;

    @Autowired
    private com.example.demo.repository.OrderRepository orderRepository;

    // ========== CHECKOUT (ÖDEME) SAYFASI ==========
    
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName(); // Email (username olarak kullanılıyor)
            Optional<User> userOptional = userService.getUserByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                model.addAttribute("addresses", user.getAddresses());
            } else {
                // User not found in DB, though authenticated. Should ideally not happen.
                return "redirect:/auth/login"; 
            }
        } else {
            // Not authenticated
            return "redirect:/auth/login";
        }
        return "checkout";
    }

    // ========== ÜRÜN GÖRÜNTÜLEME (VIEW) ==========
    
    // Ürün listesi sayfası
    @GetMapping("/products")
    public String showProductsPage(Model model) {
        List<Coffee> coffees = productService.getAllCoffees();
        List<Dessert> desserts = productService.getAllDesserts();
        model.addAttribute("coffees", coffees);
        model.addAttribute("desserts", desserts);
        return "products";
    }

    // ========== API ENDPOINT'LERİ (JSON) ==========
    
    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> getActiveProducts() {
        return productService.getActiveProducts();
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public Optional<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/api/products/coffees")
    @ResponseBody
    public List<Coffee> getAllCoffees() {
        return productService.getAllCoffees();
    }

    @GetMapping("/api/products/desserts")
    @ResponseBody
    public List<Dessert> getAllDesserts() {
        return productService.getAllDesserts();
    }

    // ========== SİPARİŞ İŞLEMLERİ ==========
    
    @PostMapping("/api/orders")
    @ResponseBody
    public OrderResponseDTO createOrder(@RequestBody OrderDTO orderDTO) {
        System.out.println("=== Sipariş Alındı ===");
        System.out.println("Kullanıcı ID: " + (orderDTO.getUser() != null ? orderDTO.getUser().getId() : "null"));
        System.out.println("Adres ID: " + (orderDTO.getShippingAddress() != null ? orderDTO.getShippingAddress().getId() : "null"));
        System.out.println("Ödeme Yöntemi: " + orderDTO.getPaymentMethod());
        System.out.println("Ürün Sayısı: " + (orderDTO.getItems() != null ? orderDTO.getItems().size() : 0));
        
        try {
            // Order objesi oluştur
            Order order = new Order();
            
            // User'ı al
            Optional<User> userOpt = userService.getUserById(orderDTO.getUser().getId());
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Kullanıcı bulunamadı");
            }
            order.setUser(userOpt.get());
            
            // Address'i al
            if (orderDTO.getShippingAddress() == null || orderDTO.getShippingAddress().getId() == null) {
                throw new RuntimeException("Teslimat adresi seçilmedi");
            }
            Optional<Address> addressOpt = addressRepository.findById(orderDTO.getShippingAddress().getId());
            if (addressOpt.isEmpty()) {
                throw new RuntimeException("Adres bulunamadı");
            }
            order.setShippingAddress(addressOpt.get());
            
            // Ödeme yöntemi
            order.setPaymentMethod(orderDTO.getPaymentMethod());
            
            // Order Items'ları oluştur
            List<OrderItem> items = new ArrayList<>();
            if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
                for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                    // Ürünü veritabanından al
                    Optional<Product> productOpt = productService.getProductById(itemDTO.getProductId());
                    if (productOpt.isEmpty()) {
                        System.err.println("⚠️ Ürün bulunamadı: " + itemDTO.getProductId());
                        continue;
                    }
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(productOpt.get());
                    orderItem.setQuantity(itemDTO.getQuantity());
                    orderItem.setSelectedSize(itemDTO.getSelectedSize());
                    orderItem.setSubTotal(itemDTO.getSubTotal());
                    orderItem.setOrder(order);
                    
                    items.add(orderItem);
                }
            }
            order.setItems(items);
            
            // Siparişi kaydet
            Order createdOrder = orderService.createOrder(order);
            
            System.out.println("✅ Sipariş Başarılı - Sipariş ID: " + createdOrder.getId());
            System.out.println("Durum: " + createdOrder.getStatus());
            System.out.println("Toplam: ₺" + createdOrder.getTotalAmount());
            
            // Basit bir response döndür (sonsuz döngüyü önlemek için)
            return new OrderResponseDTO(
                createdOrder.getId(),
                createdOrder.getStatus(),
                createdOrder.getTotalAmount(),
                "Sipariş başarıyla oluşturuldu"
            );
        } catch (Exception e) {
            System.err.println("❌ Sipariş oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Sipariş oluşturulamadı: " + e.getMessage(), e);
        }
    }

    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public Optional<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // ========== KULLANICI PROFİL SAYFASI ==========

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        // SecurityContext'ten giriş yapan kullanıcının bilgisini al
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName(); // Email (username olarak kullanılıyor)
            Optional<User> userOptional = userService.getUserByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                
                // Siparişleri getir
                List<Order> orders = orderRepository.findByUserOrderByCreatedDateDesc(user);
                model.addAttribute("orders", orders);
                
                return "profile";
            }
        }
        return "redirect:/auth/login";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam Long userId,
                                @RequestParam String name,
                                @RequestParam String surname,
                                @RequestParam(required = false) String email,
                                Model model) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setName(name);
                user.setSurname(surname);
                userService.updateOwnProfile(userId, user);
                model.addAttribute("success", "Profil başarıyla güncellendi!");
            }
            model.addAttribute("user", userService.getUserById(userId).get());
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Profil güncellemesi başarısız!");
            model.addAttribute("user", userService.getUserById(userId).get());
            return "profile";
        }
    }

    @PostMapping("/profile/add-address")
    public String addAddress(@RequestParam Long userId,
                            @RequestParam String addressType,
                            @RequestParam String street,
                            @RequestParam String city,
                            @RequestParam String district,
                            @RequestParam String postalCode,
                            @RequestParam String openAddress,
                            Model model) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Address address = new Address();
                address.setAddressType(addressType);
                address.setStreet(street);
                address.setCity(city);
                address.setDistrict(district);
                address.setPostalCode(postalCode);
                address.setOpenAddress(openAddress);
                address.setUser(user);
                user.getAddresses().add(address);
                userService.updateOwnProfile(userId, user);
                model.addAttribute("success", "Adres başarıyla eklendi!");
            }
            model.addAttribute("user", userService.getUserById(userId).get());
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Adres eklenemedi: " + e.getMessage());
            model.addAttribute("user", userService.getUserById(userId).get());
            return "profile";
        }
    }

    // Checkout sayfasından AJAX ile adres ekleme
    @PostMapping("/api/add-address")
    @ResponseBody
    public Address addAddressApi(@RequestParam String addressType,
                                 @RequestParam String street,
                                 @RequestParam String city,
                                 @RequestParam String district,
                                 @RequestParam String postalCode,
                                 @RequestParam String openAddress) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Email (username olarak kullanılıyor)
        User user = userService.getUserByEmail(email)
                              .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        
        Address address = new Address();
        address.setAddressType(addressType);
        address.setStreet(street);
        address.setCity(city);
        address.setDistrict(district);
        address.setPostalCode(postalCode);
        address.setOpenAddress(openAddress);
        address.setUser(user);
        
        return addressRepository.save(address);
    }

    @PostMapping("/profile/delete-address")
    public String deleteAddress(@RequestParam Long userId, @RequestParam Long addressId, Model model) {
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            Optional<Address> addressOpt = addressRepository.findById(addressId);
            
            if (addressOpt.isPresent() && addressOpt.get().getUser().getId().equals(userId)) {
                addressRepository.deleteById(addressId);
                model.addAttribute("success", "Adres başarıyla silindi!");
            } else {
                model.addAttribute("error", "Adres silinemedi veya yetkiniz yok.");
            }
            
            // Güncel kullanıcı bilgisini tekrar yükle
            User updatedUser = userService.getUserById(userId).get();
            model.addAttribute("user", updatedUser);
            
            // Siparişleri tekrar yükle
            List<Order> orders = orderRepository.findByUserOrderByCreatedDateDesc(updatedUser);
            model.addAttribute("orders", orders);
            
            return "profile";
        }
        return "redirect:/auth/login";
    }

    @PostMapping("/profile/update-address")
    public String updateAddress(@RequestParam Long userId,
                                @RequestParam Long addressId,
                                @RequestParam String addressType,
                                @RequestParam String street,
                                @RequestParam String city,
                                @RequestParam String district,
                                @RequestParam String postalCode,
                                @RequestParam String openAddress,
                                Model model) {
        
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            Optional<Address> addressOpt = addressRepository.findById(addressId);
            
            if (addressOpt.isPresent() && addressOpt.get().getUser().getId().equals(userId)) {
                Address address = addressOpt.get();
                address.setAddressType(addressType);
                address.setStreet(street);
                address.setCity(city);
                address.setDistrict(district);
                address.setPostalCode(postalCode);
                address.setOpenAddress(openAddress);
                addressRepository.save(address);
                model.addAttribute("success", "Adres başarıyla güncellendi!");
            } else {
                model.addAttribute("error", "Adres güncellenemedi.");
            }
            
            // Güncel kullanıcı bilgisini tekrar yükle
            User updatedUser = userService.getUserById(userId).get();
            model.addAttribute("user", updatedUser);
            
            // Siparişleri tekrar yükle
            List<Order> orders = orderRepository.findByUserOrderByCreatedDateDesc(updatedUser);
            model.addAttribute("orders", orders);
            
            return "profile";
        }
        return "redirect:/auth/login";
    }
    
    // Kullanıcı kendi bilgilerini görüntüle
    @GetMapping("/api/profile/{userId}")
    @ResponseBody
    public Optional<User> getOwnProfile(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    // Kullanıcı kendi bilgilerini güncelleyebilmeli (Email değiştirilemez)
    @PutMapping("/api/profile/{userId}")
    @ResponseBody
    public User updateOwnProfile(@PathVariable Long userId, @RequestBody User updatedUser) {
        return userService.updateOwnProfile(userId, updatedUser);
    }

    // Kullanıcı kendi şifresini değiştir
    @PutMapping("/api/profile/{userId}/password")
    @ResponseBody
    public String changeOwnPassword(
            @PathVariable Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return userService.changeOwnPassword(userId, oldPassword, newPassword);
    }

    // Not: Müşteri sadece kendi siparişlerini görebilir
  
}

