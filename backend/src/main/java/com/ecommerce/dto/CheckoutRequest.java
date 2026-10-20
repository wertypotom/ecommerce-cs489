package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
