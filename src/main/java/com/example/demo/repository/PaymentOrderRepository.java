package com.example.demo.repository;

import com.example.demo.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
    List<PaymentOrder> findAllByRazorpayOrderId(String razorpayOrderId);
    boolean existsByUserIdAndGameIdAndStatus(Long userId, Long gameId, String status);
    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentOrder p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end")
    Double sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end")
    Long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentOrder p WHERE p.status = 'PAID'")
    Double sumAllRevenue();

    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.status = 'PAID'")
    Long countAllOrders();
}