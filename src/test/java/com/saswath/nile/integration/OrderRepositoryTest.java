package com.saswath.nile.integration;

import com.saswath.nile.entity.*;
import com.saswath.nile.repository.OrderRepository;
import com.saswath.nile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository — Integration Tests")
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        alice = new User("Alice", "alice@nile.com", "encoded");
        alice.setRole("ROLE_USER");
        alice = userRepository.save(alice);

        bob = new User("Bob", "bob@nile.com", "encoded");
        bob.setRole("ROLE_USER");
        bob = userRepository.save(bob);
    }

    @Test
    @DisplayName("findByUser_Id returns all orders belonging to the user")
    void findByUserId_returnsOrdersForUser() {
        orderRepository.save(new Order(alice, new BigDecimal("19.98"), OrderStatus.CREATED));
        orderRepository.save(new Order(alice, new BigDecimal("9.99"), OrderStatus.CREATED));

        List<Order> orders = orderRepository.findByUser_Id(alice.getId());

        assertThat(orders).hasSize(2);
        orders.forEach(o -> assertThat(o.getUser().getId()).isEqualTo(alice.getId()));
    }

    @Test
    @DisplayName("findByUser_Id does not return orders belonging to other users")
    void findByUserId_doesNotReturnOtherUsersOrders() {
        orderRepository.save(new Order(alice, new BigDecimal("19.98"), OrderStatus.CREATED));
        orderRepository.save(new Order(bob, new BigDecimal("5.00"), OrderStatus.CREATED));

        List<Order> aliceOrders = orderRepository.findByUser_Id(alice.getId());

        assertThat(aliceOrders).hasSize(1);
        assertThat(aliceOrders.get(0).getTotalAmount()).isEqualByComparingTo("19.98");
    }

    @Test
    @DisplayName("findByUser_Id returns empty list when user has no orders")
    void findByUserId_returnsEmpty_whenNoOrders() {
        List<Order> orders = orderRepository.findByUser_Id(alice.getId());

        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("findByStatus returns only orders with the matching status")
    void findByStatus_returnsMatchingOrders() {
        orderRepository.save(new Order(alice, new BigDecimal("19.98"), OrderStatus.CREATED));
        orderRepository.save(new Order(bob, new BigDecimal("5.00"), OrderStatus.CREATED));

        List<Order> created = orderRepository.findByStatus(OrderStatus.CREATED);

        assertThat(created).hasSize(2);
        created.forEach(o -> assertThat(o.getStatus()).isEqualTo(OrderStatus.CREATED));
    }

    @Test
    @DisplayName("findByStatus returns empty list when no orders match the status")
    void findByStatus_returnsEmpty_whenNoMatch() {
        orderRepository.save(new Order(alice, new BigDecimal("19.98"), OrderStatus.CREATED));

        // No SHIPPED orders exist
        List<Order> shipped = orderRepository.findByStatus(OrderStatus.SHIPPED);

        assertThat(shipped).isEmpty();
    }

    @Test
    @DisplayName("saved order persists totalAmount and status correctly")
    void save_persistsOrderFields() {
        Order saved = orderRepository.save(
                new Order(alice, new BigDecimal("34.97"), OrderStatus.CREATED));

        Order found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTotalAmount()).isEqualByComparingTo("34.97");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(found.getCreatedAt()).isNotNull();
    }
}