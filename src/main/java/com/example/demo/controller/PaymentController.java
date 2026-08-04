package com.example.demo.controller;

import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public Map<String, Object> createOrder(Authentication authentication, @RequestBody Map<String, Object> body) throws RazorpayException {
        Double amount = Double.valueOf(body.get("amount").toString());

        List<?> rawGameIds = (List<?>) body.get("gameIds");
        List<Long> gameIds = rawGameIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());

        JSONObject order = paymentService.createOrder(authentication.getName(), amount, gameIds);
        return order.toMap();
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, String> body) {
        boolean valid = paymentService.verifyPayment(
                body.get("razorpay_order_id"),
                body.get("razorpay_payment_id"),
                body.get("razorpay_signature")
        );
        return Map.of("success", valid);
    }

    @GetMapping("/access/{gameId}")
    public Map<String, Object> checkAccess(Authentication authentication, @PathVariable Long gameId) {
        boolean hasAccess = paymentService.hasAccess(authentication.getName(), gameId);
        return Map.of("hasAccess", hasAccess);
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getOrderHistory(Authentication authentication) {
        return paymentService.getOrderHistory(authentication.getName());
    }
    @GetMapping("/stats")
public Map<String, Object> getUserStats(Authentication authentication) {
    return paymentService.getUserStats(authentication.getName());
}
}