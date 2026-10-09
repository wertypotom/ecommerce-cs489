package com.ecommerce.controller;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartRequest;
import com.ecommerce.dto.CartResponse;
import jakarta.validation.Valid;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable Long id) {
        CartResponse response = new CartResponse(id, 1L, List.of(), BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartByUserId(@PathVariable Long userId) {
        CartResponse response = new CartResponse(1L, userId, List.of(), BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CartRequest request) {
        CartResponse response = new CartResponse(1L, request.getUserId(), List.of(), BigDecimal.ZERO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemResponse> addItemToCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = new CartItemResponse(1L, request.getProductId(), "Product Name", 
                request.getQuantity(), new BigDecimal("99.99"), new BigDecimal("99.99"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(@PathVariable Long cartId, @PathVariable Long itemId, @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = new CartItemResponse(itemId, request.getProductId(), "Product Name", 
                request.getQuantity(), new BigDecimal("99.99"), new BigDecimal("99.99"));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long itemId) {
        return ResponseEntity.noContent().build();
    }
}
