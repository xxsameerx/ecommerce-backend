package com.example.demo.service;

import com.example.demo.model.PaymentOrder;
import com.example.demo.model.User;
import com.example.demo.repository.PaymentOrderRepository;
import com.example.demo.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private UserRepository userRepository;

    public JSONObject createOrder(String email, Double amount, List<Long> gameIds) throws RazorpayException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        Map<String, Object> orderRequestMap = new HashMap<>();
        orderRequestMap.put("amount", Math.round(amount * 100));
        orderRequestMap.put("currency", "INR");
        orderRequestMap.put("receipt", "receipt_" + System.currentTimeMillis());

        JSONObject orderRequest = new JSONObject(orderRequestMap);

        Order order = razorpay.orders.create(orderRequest);

        String razorpayOrderId = order.get("id").toString();
        Object orderAmount = order.get("amount");
        Object orderCurrency = order.get("currency");

        for (Long gameId : gameIds) {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setUserId(user.getUserId());
            paymentOrder.setGameId(gameId);
            paymentOrder.setRazorpayOrderId(razorpayOrderId);
            paymentOrder.setAmount(amount);
            paymentOrderRepository.save(paymentOrder);
        }

        logger.info("Order created: {} for user {} amount {} games {}", razorpayOrderId, email, amount, gameIds);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("orderId", razorpayOrderId);
        responseMap.put("amount", orderAmount);
        responseMap.put("currency", orderCurrency);
        responseMap.put("keyId", keyId);

        return new JSONObject(responseMap);
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            Map<String, String> optionsMap = new HashMap<>();
            optionsMap.put("razorpay_order_id", orderId);
            optionsMap.put("razorpay_payment_id", paymentId);
            optionsMap.put("razorpay_signature", signature);

            JSONObject options = new JSONObject(optionsMap);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByRazorpayOrderId(orderId);

            if (paymentOrders.isEmpty()) {
                logger.error("No orders found for orderId: {}", orderId);
                return false;
            }

            for (PaymentOrder po : paymentOrders) {
                po.setRazorpayPaymentId(paymentId);
                po.setRazorpaySignature(signature);
                po.setStatus(isValid ? "PAID" : "FAILED");
                paymentOrderRepository.save(po);
            }

            logger.info("Payment verification for order {}: {}", orderId, isValid ? "SUCCESS" : "FAILED");

            return isValid;
        } catch (Exception e) {
            logger.error("Payment verification error for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    public boolean hasAccess(String email, Long gameId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentOrderRepository.existsByUserIdAndGameIdAndStatus(user.getUserId(), gameId, "PAID");
    }

    public List<Map<String, Object>> getOrderHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PaymentOrder> orders = paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (PaymentOrder o : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", o.getId());
            map.put("gameId", o.getGameId());
            map.put("razorpayOrderId", o.getRazorpayOrderId());
            map.put("razorpayPaymentId", o.getRazorpayPaymentId());
            map.put("amount", o.getAmount());
            map.put("currency", o.getCurrency());
            map.put("status", o.getStatus());
            map.put("createdAt", o.getCreatedAt());
            result.add(map);
        }
        return result;
    }
    public Map<String, Object> getUserStats(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    List<PaymentOrder> orders = paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

    double totalSpent = 0;
    int gamesOwned = 0;
    int totalOrders = 0;
    java.util.Set<Long> uniqueGameIds = new java.util.HashSet<>();

    for (PaymentOrder o : orders) {
        if ("PAID".equals(o.getStatus())) {
            totalSpent += o.getAmount();
            uniqueGameIds.add(o.getGameId());
            totalOrders++;
        }
    }
    gamesOwned = uniqueGameIds.size();

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalSpent", totalSpent);
    stats.put("gamesOwned", gamesOwned);
    stats.put("totalOrders", totalOrders);
    stats.put("memberSince", orders.isEmpty() ? null : orders.get(orders.size() - 1).getCreatedAt());
    return stats;
}
}