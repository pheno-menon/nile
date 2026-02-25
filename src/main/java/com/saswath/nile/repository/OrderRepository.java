package com.saswath.nile.repository;

import com.saswath.nile.entity.Order;
import com.saswath.nile.entity.OrderStatus;
import com.saswath.nile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUser_Id(Long userId);
}

