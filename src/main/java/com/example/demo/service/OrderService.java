package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // siparişleri getir (Admin için)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // kullanıcının siparişlerini getir
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    // ID'ye göre sipariş getir
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // sipariş oluştur - PREPARING durumunda başlar
    @Transactional
    public Order createOrder(Order order) {
        try {
            if (order.getCreatedDate() == null) {
                order.setCreatedDate(new Date());
            }

            // Sipariş oluşturulduğunda PREPARING (Hazırlanıyor) durumuna geçer
            if (order.getStatus() == null) {
                order.setStatus(OrderStatus.PREPARING);
            }

            // Başlangıç toplam tutarı
            if (order.getTotalAmount() == null) {
                order.setTotalAmount(0.0);
            }

            // Sipariş detaylarını kaydet ve toplamı hesapla
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    item.setOrder(order);
                    if (item.getQuantity() <= 0) {
                        item.setQuantity(1);
                    }
                    item.calculateSubTotal();

                    // STOK DÜŞÜRME: Ürün stoğundan sipariş miktarını düş
                    Product product = item.getProduct();
                    if (product != null) {
                        int newStock = product.getStock() - item.getQuantity();
                        if (newStock < 0) {
                            throw new RuntimeException("Yetersiz stok: " + product.getName() +
                                    " (Mevcut: " + product.getStock() + ", İstenen: " + item.getQuantity() + ")");
                        }
                        product.setStock(newStock);
                        productRepository.save(product);
                    }
                }
            }

            order.calculateTotal();
            Order savedOrder = orderRepository.save(order);

            // Item'ları kaydet
            if (savedOrder.getItems() != null && !savedOrder.getItems().isEmpty()) {
                orderItemRepository.saveAll(savedOrder.getItems());
            }

            return savedOrder;
        } catch (Exception e) {
            System.err.println("Sipariş oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Sipariş oluşturulamadı: " + e.getMessage(), e);
        }
    }

    // sipariş güncelle
    public Order updateOrder(Order order) {
        // Sipariş detaylarını güncelle
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                item.calculateSubTotal();
                orderItemRepository.save(item);
            }
        }

        order.calculateTotal();
        return orderRepository.save(order);
    }

    // sipariş durumunu güncelle
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }

    // sipariş sil
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // duruma göre siparişleri getir
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    // Sipariş iptal et - stokları geri yükle
    @Transactional
    public Order cancelOrder(Long orderId) {
        // FETCH JOIN ile sipariş ve detaylarını (items + product) birlikte getir
        Optional<Order> orderOptional = orderRepository.findByIdWithItems(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Sipariş bulunamadı: " + orderId);
        }

        Order order = orderOptional.get();

        // Sadece PREPARING durumundaki siparişler iptal edilebilir
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("Sadece 'Hazırlanıyor' durumundaki siparişler iptal edilebilir. Mevcut durum: "
                    + order.getStatus());
        }

        // Stokları geri yükle
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                    System.out.println("Stok geri yüklendi: " + product.getName() + " (+" + item.getQuantity() + ")");
                }
            }
        }

        // Durumu CANCELLED olarak güncelle
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        System.out.println("Sipariş iptal edildi: #" + orderId);
        return cancelledOrder;
    }

    // 15 saniye sonra otomatik olarak teslim edildi durumuna geç
    public void scheduleDelivery(Long orderId) {
        scheduler.schedule(() -> {
            updateOrderStatus(orderId, OrderStatus.DELIVERED);
        }, 15, TimeUnit.SECONDS);
    }

    // günlük ciroyu hesaplama
    public double getDailyTurnover() {
        // Bugünü temsil eden takvim nesnesi
        java.util.Date today = new java.util.Date();

        // gün başlangıcını ve bitişini ayarla
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date startOfDay = calendar.getTime();

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);
        java.util.Date endOfDay = calendar.getTime();

        List<Order> dailyOrders = orderRepository.findByStatusAndCreatedDateBetween(OrderStatus.DELIVERED, startOfDay,
                endOfDay);

        return dailyOrders.stream().mapToDouble(Order::getTotalAmount).sum();
    }
}
