package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private BigDecimal total;

    public CartResponse() {
    }

    public CartResponse(Long id, Long userId, List<CartItemDto> items, BigDecimal total) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
