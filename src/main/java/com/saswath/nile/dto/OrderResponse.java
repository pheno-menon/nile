package com.saswath.nile.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;

    public OrderResponse(Long id, BigDecimal totalAmount, String status, LocalDateTime orderDate, List<OrderItemResponse> items) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.items = items;
    }

    public Long getId() { return id; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public String getStatus() { return status; }

    public LocalDateTime getOrderDate() { return orderDate; }

    public List<OrderItemResponse> getItems() { return items; }
}
