package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderItemResponse;
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
@RequestMapping("/api/order-items")
public class OrderItemController {

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable Long id) {
        OrderItemResponse response = new OrderItemResponse(id, 1L, "Product Name", 2, 
                new BigDecimal("99.99"), new BigDecimal("199.98"));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> createOrderItem(@Valid @RequestBody OrderItemRequest request) {
        OrderItemResponse response = new OrderItemResponse(1L, request.getProductId(), "Product Name", 
                request.getQuantity(), request.getPriceSnapshot(), 
                request.getPriceSnapshot().multiply(BigDecimal.valueOf(request.getQuantity())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemResponse> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItemRequest request) {
        OrderItemResponse response = new OrderItemResponse(id, request.getProductId(), "Product Name", 
                request.getQuantity(), request.getPriceSnapshot(), 
                request.getPriceSnapshot().multiply(BigDecimal.valueOf(request.getQuantity())));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(List.of());
    }
}
