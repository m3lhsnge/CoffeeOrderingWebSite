package com.example.demo.repository;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Kullanıcının tüm siparişlerini getir
    List<Order> findByUser(User user);

    // Kullanıcının siparişlerini tarihe göre tersten getir (En yeni en üstte)
    List<Order> findByUserOrderByCreatedDateDesc(User user);

    // Sipariş durumuna göre getir
    List<Order> findByStatus(OrderStatus status);

    // Kullanıcıya ve duruma göre getir
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    // Durum ve tarih aralığına göre siparişleri getir
    List<Order> findByStatusAndCreatedDateBetween(OrderStatus status, java.util.Date startOfDay,
            java.util.Date endOfDay);

    // Sipariş ve detaylarını (items + product) birlikte getir - iptal işlemi için
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
