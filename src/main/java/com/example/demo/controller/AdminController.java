package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    // ========== ADMIN PANELI (PAGE ENDPOINTS) ==========

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("usersCount", userService.getAllUsers().size());
        model.addAttribute("ordersCount", orderService.getAllOrders().size());
        model.addAttribute("productsCount", productService.getAllProducts().size());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(5).toList());

        // Günlük ciroyu ekle
        double dailyTurnover = orderService.getDailyTurnover();
        model.addAttribute("dailyTurnover", dailyTurnover);
        return "admin-dashboard";
    }

    @GetMapping("/users-management")
    public String showUsersManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @GetMapping("/products-management")
    public String showProductsManagement(Model model) {
        model.addAttribute("coffees", productService.getAllCoffees());
        model.addAttribute("desserts", productService.getAllDesserts());
        return "admin-products";
    }

    @PostMapping("/users/add")
    public String addUserPage(@RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {
        try {
            User user = new User();
            user.setName(name);
            user.setSurname(surname);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(Role.valueOf(role));
            user.setEnabled(true);
            userService.addUser(user);
            model.addAttribute("success", "Kullanıcı başarıyla eklendi!");
        } catch (Exception e) {
            model.addAttribute("error", "Kullanıcı eklenemedi: " + e.getMessage());
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/users/update/{id}")
    public String updateUserPage(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam String role,
            Model model) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setName(name);
                user.setSurname(surname);
                // Email değiştirmiyoruz (Unique constraint ve güvenlik için)
                user.setRole(Role.valueOf(role));
                userService.updateUser(user);
                model.addAttribute("success", "Kullanıcı başarıyla güncellendi!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Kullanıcı güncellenemedi: " + e.getMessage());
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUserPage(@PathVariable Long id, Model model) {
        try {
            userService.deleteUser(id);
            model.addAttribute("success", "Kullanıcı başarıyla deaktive edildi!"); // Mesaj güncellendi
        } catch (Exception e) {
            model.addAttribute("error", "Kullanıcı deaktive edilemedi: " + e.getMessage());
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/products/add-coffee")
    public String addCoffeePage(@RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stock,
            @RequestParam String origin,
            @RequestParam String roastLevel,
            Model model) {
        try {
            Coffee coffee = new Coffee();
            coffee.setName(name);
            coffee.setDescription(description);
            coffee.setPrice(price);
            coffee.setStock(stock);
            coffee.setOrigin(origin);
            coffee.setRoastLevel(roastLevel);
            coffee.setActive(true);
            productService.addCoffee(coffee);
            model.addAttribute("success", "Kahve başarıyla eklendi!");
        } catch (Exception e) {
            model.addAttribute("error", "Kahve eklenemedi: " + e.getMessage());
        }
        model.addAttribute("coffees", productService.getAllCoffees());
        model.addAttribute("desserts", productService.getAllDesserts());
        return "admin-products";
    }

    @PostMapping("/products/add-dessert")
    public String addDessertPage(@RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stock,
            @RequestParam(required = false) Integer calories,
            @RequestParam(required = false, defaultValue = "false") boolean glutenFree,
            Model model) {
        try {
            Dessert dessert = new Dessert();
            dessert.setName(name);
            dessert.setDescription(description);
            dessert.setPrice(price);
            dessert.setStock(stock);
            dessert.setCalories(calories);
            dessert.setGlutenFree(glutenFree);
            dessert.setActive(true);
            productService.addDessert(dessert);
            model.addAttribute("success", "Tatlı başarıyla eklendi!");
        } catch (Exception e) {
            model.addAttribute("error", "Tatlı eklenemedi: " + e.getMessage());
        }
        model.addAttribute("coffees", productService.getAllCoffees());
        model.addAttribute("desserts", productService.getAllDesserts());
        return "admin-products";
    }

    @PostMapping("/products/update/{id}")
    public String updateProductPage(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stock,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String roastLevel,
            Model model) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setName(name);
                product.setDescription(description);
                product.setPrice(price);
                product.setStock(stock);
                if (product instanceof Coffee) {
                    Coffee coffee = (Coffee) product;
                    if (origin != null)
                        coffee.setOrigin(origin);
                    if (roastLevel != null)
                        coffee.setRoastLevel(roastLevel);
                }
                productService.updateProduct(product);
                model.addAttribute("success", "Ürün başarıyla güncellendi!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ürün güncellenemedi: " + e.getMessage());
        }
        model.addAttribute("coffees", productService.getAllCoffees());
        model.addAttribute("desserts", productService.getAllDesserts());
        return "admin-products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProductPage(@PathVariable Long id, Model model) {
        try {
            productService.deleteProduct(id);
            model.addAttribute("success", "Ürün başarıyla silindi!");
        } catch (Exception e) {
            model.addAttribute("error", "Ürün silinemedi: " + e.getMessage());
        }
        model.addAttribute("coffees", productService.getAllCoffees());
        model.addAttribute("desserts", productService.getAllDesserts());
        return "admin-products";
    }

    // ========== KULLANICI YÖNETİMİ ==========

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/users")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "Kullanıcı silindi!";
    }

    // ========== ÜRÜN YÖNETİMİ ==========

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/products/coffee")
    public Coffee addCoffee(@RequestBody Coffee coffee) {
        return productService.addCoffee(coffee);
    }

    @PostMapping("/products/dessert")
    public Dessert addDessert(@RequestBody Dessert dessert) {
        return productService.addDessert(dessert);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Ürün silindi!";
    }

    @PutMapping("/products/{id}/stock")
    public String updateStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.updateStock(id, quantity);
        return "Stok güncellendi!";
    }

    @GetMapping("/products/coffees")
    public List<Coffee> getAllCoffees() {
        return productService.getAllCoffees();
    }

    @GetMapping("/products/desserts")
    public List<Dessert> getAllDesserts() {
        return productService.getAllDesserts();
    }

    // ========== SİPARİŞ YÖNETİMİ (VIEW) ==========

    // Admin sipariş yönetim sayfası
    @GetMapping("/orders")
    public String showOrdersPage(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin-orders";
    }

    // ========== SİPARİŞ YÖNETİMİ (API) ==========

    @GetMapping("/api/orders")
    @ResponseBody
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public Optional<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/orders/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        return orderService.updateOrder(order);
    }

    @PutMapping("/orders/{id}/status")
    @ResponseBody
    public Order updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(id, status);
    }

    // Siparişi yolda durumuna geçir (Admin onayı)
    @PostMapping("/orders/{id}/approve")
    @ResponseBody
    public Order approveOrder(@PathVariable Long id) {
        Order order = orderService.updateOrderStatus(id, OrderStatus.OUT_FOR_DELIVERY);
        // 15 saniye sonra otomatik teslim edildi durumuna geç
        orderService.scheduleDelivery(order.getId());
        return order;
    }

    // Siparişi iptal et (stokları geri yükler)
    @PostMapping("/orders/{id}/cancel")
    @ResponseBody
    public Order cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @DeleteMapping("/orders/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "Sipariş silindi!";
    }

    @GetMapping("/orders/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }
}