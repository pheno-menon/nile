package com.saswath.nile.unit;

import com.saswath.nile.dto.OrderResponse;
import com.saswath.nile.entity.*;
import com.saswath.nile.exception.ResourceNotFoundException;
import com.saswath.nile.repository.*;
import com.saswath.nile.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — Unit Tests")
class OrderServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User("Bob", "bob@nile.com", "encoded");
        user.setId(1L);
        user.setRole("ROLE_USER");

        product = new Product("Widget", new BigDecimal("9.99"), 10);
        product.setId(1L);

        cartItem = new CartItem(2, product, user);
        cartItem.setId(1L);
    }

    @Test
    @DisplayName("placeOrder creates order, deducts stock, clears cart, and returns response")
    void placeOrder_success() {
        Order savedOrder = new Order(user, new BigDecimal("19.98"), OrderStatus.CREATED);
        savedOrder.setId(1L);
        savedOrder.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrder));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.placeOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CREATED");
        // stock should have been decremented and saved
        assertThat(product.getStockQuantity()).isEqualTo(8);
        verify(cartItemRepository).deleteAll(List.of(cartItem));
    }

    @Test
    @DisplayName("placeOrder throws ResourceNotFoundException when user not found")
    void placeOrder_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("placeOrder throws ResourceNotFoundException when cart is empty")
    void placeOrder_throws_whenCartIsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    @DisplayName("placeOrder throws RuntimeException when a cart item exceeds product stock")
    void placeOrder_throws_whenInsufficientStock() {
        // cart requests 20 but product only has 10
        cartItem = new CartItem(20, product, user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.placeOrder(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("placeOrder calculates total amount correctly across multiple cart items")
    void placeOrder_calculatesTotal_forMultipleItems() {
        Product p2 = new Product("Gadget", new BigDecimal("5.00"), 20);
        p2.setId(2L);
        CartItem item2 = new CartItem(3, p2, user);  // 3 × 5.00 = 15.00

        // total = (2 × 9.99) + (3 × 5.00) = 19.98 + 15.00 = 34.98
        Order savedOrder = new Order(user, new BigDecimal("34.98"), OrderStatus.CREATED);
        savedOrder.setId(1L);
        savedOrder.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem, item2));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrder));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.placeOrder(1L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("34.98");
    }

    @Test
    @DisplayName("getUserOrders returns list of order responses for the user")
    void getUserOrders_returnsOrders() {
        Order order = new Order(user, new BigDecimal("19.98"), OrderStatus.CREATED);
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser_Id(1L)).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getUserOrders(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUserOrders returns empty list when user has no orders")
    void getUserOrders_returnsEmpty_whenNoOrders() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser_Id(1L)).thenReturn(List.of());

        assertThat(orderService.getUserOrders(1L)).isEmpty();
    }

    @Test
    @DisplayName("getUserOrders throws ResourceNotFoundException when user does not exist")
    void getUserOrders_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getUserOrders(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }
}