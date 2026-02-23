package com.saswath.ecommerce.entity;

public enum OrderStatus {
    CREATED("Order has been created"),
    PAID("Payment completed"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order has been delivered successfully to customer"),
    CANCELLED("Order was cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
