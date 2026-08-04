package com.example.demo.controller;

import com.example.demo.model.Cart;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Cart addToCart(Authentication authentication, @RequestParam Long gameId, @RequestParam Integer quantity) {
        return cartService.addToCart(authentication.getName(), gameId, quantity);
    }

    @GetMapping
    public Cart getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @DeleteMapping("/{cartItemId}")
    public String removeItem(Authentication authentication, @PathVariable Long cartItemId) {
        cartService.removeItem(authentication.getName(), cartItemId);
        return "Item removed";
    }

    @PutMapping("/{cartItemId}")
    public Cart updateQuantity(Authentication authentication, @PathVariable Long cartItemId, @RequestParam Integer quantity) {
        return cartService.updateQuantity(authentication.getName(), cartItemId, quantity);
    }
}