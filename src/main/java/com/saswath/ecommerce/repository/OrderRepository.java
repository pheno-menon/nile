package com.saswath.ecommerce.repository;

import com.saswath.ecommerce.entity.Order;
import com.saswath.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerName(String customerName);
}

