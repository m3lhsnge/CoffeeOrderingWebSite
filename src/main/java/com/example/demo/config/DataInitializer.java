package com.example.demo.config;

import com.example.demo.model.Coffee;
import com.example.demo.model.Dessert;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CoffeeRepository;
import com.example.demo.repository.DessertRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CoffeeRepository coffeeRepository;

    @Autowired
    private DessertRepository dessertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Eğer kahveler yoksa ekle
        if (coffeeRepository.count() == 0) {
            // Latte
            Coffee latte = new Coffee();
            latte.setName("Latte");
            latte.setPrice(45.0);
            latte.setDescription("Sütlü ve yumuşak içimli espresso");
            latte.setStock(50);
            latte.setActive(true);
            latte.setOrigin("İtalya");
            latte.setRoastLevel("Medium");
            coffeeRepository.save(latte);

            // Espresso
            Coffee espresso = new Coffee();
            espresso.setName("Espresso");
            espresso.setPrice(35.0);
            espresso.setDescription("Yoğun ve güçlü klasik espresso");
            espresso.setStock(50);
            espresso.setActive(true);
            espresso.setOrigin("İtalya");
            espresso.setRoastLevel("Dark");
            coffeeRepository.save(espresso);

            // Mocha
            Coffee mocha = new Coffee();
            mocha.setName("Mocha");
            mocha.setPrice(50.0);
            mocha.setDescription("Çikolatalı ve sütlü özel karışım");
            mocha.setStock(50);
            mocha.setActive(true);
            mocha.setOrigin("Yemen");
            mocha.setRoastLevel("Medium");
            coffeeRepository.save(mocha);

            // Americano
            Coffee americano = new Coffee();
            americano.setName("Americano");
            americano.setPrice(40.0);
            americano.setDescription("Espresso ve sıcak su karışımı");
            americano.setStock(50);
            americano.setActive(true);
            americano.setOrigin("Amerika");
            americano.setRoastLevel("Medium");
            coffeeRepository.save(americano);

            System.out.println("Kahveler başarıyla eklendi!");
        }

        // Tatlıları ekle
        if (dessertRepository.count() == 0) {
            // Cheesecake
            Dessert cheesecake = new Dessert();
            cheesecake.setName("Cheesecake");
            cheesecake.setPrice(55.0);
            cheesecake.setDescription("Kremalı New York tarzı cheesecake");
            cheesecake.setStock(30);
            cheesecake.setActive(true);
            cheesecake.setCalories(380);
            cheesecake.setGlutenFree(false);
            dessertRepository.save(cheesecake);

            // Tiramisu
            Dessert tiramisu = new Dessert();
            tiramisu.setName("Tiramisu");
            tiramisu.setPrice(50.0);
            tiramisu.setDescription("İtalyan klasiği, kahveli ve maskarpone ile yapılmış");
            tiramisu.setStock(25);
            tiramisu.setActive(true);
            tiramisu.setCalories(320);
            tiramisu.setGlutenFree(false);
            dessertRepository.save(tiramisu);

            // Brownie
            Dessert brownie = new Dessert();
            brownie.setName("Çikolata Brownie");
            brownie.setPrice(40.0);
            brownie.setDescription("Yumuşak ve çikolatalı brownie, ıssız kalmayacak");
            brownie.setStock(35);
            brownie.setActive(true);
            brownie.setCalories(420);
            brownie.setGlutenFree(false);
            dessertRepository.save(brownie);

            // Glutensiz Pasta
            Dessert glutenFreeCake = new Dessert();
            glutenFreeCake.setName("Glutensiz Pasta");
            glutenFreeCake.setPrice(48.0);
            glutenFreeCake.setDescription("Glutensiz unla yapılmış nefis pasta");
            glutenFreeCake.setStock(20);
            glutenFreeCake.setActive(true);
            glutenFreeCake.setCalories(290);
            glutenFreeCake.setGlutenFree(true);
            dessertRepository.save(glutenFreeCake);

            // Panna Cotta
            Dessert pannaCotta = new Dessert();
            pannaCotta.setName("Panna Cotta");
            pannaCotta.setPrice(52.0);
            pannaCotta.setDescription("İtalyan krema tatlısı, taze meyvelerle sunuluyor");
            pannaCotta.setStock(28);
            pannaCotta.setActive(true);
            pannaCotta.setCalories(280);
            pannaCotta.setGlutenFree(true);
            dessertRepository.save(pannaCotta);

            System.out.println("Tatlılar başarıyla eklendi!");
        }

        // Admin kullanıcısı ekle
        if (userRepository.findByEmailAndDeletedFalse("admin@kahvesiparis.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("User");
            admin.setEmail("admin@kahvesiparis.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Admin kullanıcısı başarıyla eklendi! (Email: admin@kahvesiparis.com, Şifre: admin123)");
        }
    }
}