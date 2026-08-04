package com.example.demo.service;

import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(user.getUserId());
                    return cartRepository.save(cart);
                });
    }

    public Cart addToCart(String email, Long gameId, Integer quantity) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findByCart_CartIdAndGameId(cart.getCartId(), gameId)
                .orElse(new CartItem());

        if (item.getCartItemId() == null) {
            item.setCart(cart);
            item.setGameId(gameId);
            item.setQuantity(quantity);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        cartItemRepository.save(item);
        return cartRepository.findById(cart.getCartId()).orElseThrow();
    }

    public Cart getCart(String email) {
        return getOrCreateCart(email);
    }

    public void removeItem(String email, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public Cart updateQuantity(String email, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getOrCreateCart(email);
    }
}