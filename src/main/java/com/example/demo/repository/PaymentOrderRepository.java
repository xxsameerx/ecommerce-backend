package com.example.demo.repository;

import com.example.demo.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
    List<PaymentOrder> findAllByRazorpayOrderId(String razorpayOrderId);
    boolean existsByUserIdAndGameIdAndStatus(Long userId, Long gameId, String status);
    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}