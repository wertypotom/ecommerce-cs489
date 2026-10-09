package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class CartRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    public CartRequest() {
    }

    public CartRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
