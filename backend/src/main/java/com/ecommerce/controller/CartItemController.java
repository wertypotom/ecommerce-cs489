package com.ecommerce.controller;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.service.CartService;
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
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartService cartService;

    public CartItemController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getAllCartItems() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemResponse> getCartItemById(@PathVariable Long id) {
        CartItemResponse response = new CartItemResponse(id, 1L, "Product Name", 2,
                new BigDecimal("99.99"), new BigDecimal("199.98"));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> createCartItem(@Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = new CartItemResponse(1L, request.getProductId(), "Product Name",
                request.getQuantity(), new BigDecimal("99.99"), new BigDecimal("99.99"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponse> updateCartItem(@PathVariable Long id, @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = new CartItemResponse(id, request.getProductId(), "Product Name",
                request.getQuantity(), new BigDecimal("99.99"), new BigDecimal("99.99"));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
