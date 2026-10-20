package com.ecommerce.controller;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.createCart(request.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        logger.info("DELETE request received for cart id: {}", id);
        try {
            cartService.deleteCart(id);
            logger.info("Cart {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete cart {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemResponse> addItemToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(cartId, request));
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(cartId, itemId, request));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long itemId) {
        cartService.removeItem(cartId, itemId);
        return ResponseEntity.noContent().build();
    }
}
