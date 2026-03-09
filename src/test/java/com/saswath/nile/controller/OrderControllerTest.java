package com.saswath.nile.controller;

import com.saswath.nile.entity.*;
import com.saswath.nile.repository.*;
import com.saswath.nile.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OrderController — Controller Tests")
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private User alice;
    private Product product;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Delete in FK-safe order: child tables before parent tables
        cartItemRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        alice = new User("Alice", "alice@nile.com", passwordEncoder.encode("pass"));
        alice.setRole("ROLE_USER");
        alice = userRepository.save(alice);
        userToken = "Bearer " + jwtUtil.generateToken("alice@nile.com");

        product = productRepository.save(new Product("Widget", new BigDecimal("9.99"), 50));
    }

    @Test
    @DisplayName("POST /api/orders/place/{userId} returns 200 and OrderResponse when cart has items")
    void placeOrder_returns200_withItemsInCart() throws Exception {
        cartItemRepository.save(new CartItem(2, product, alice));

        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(19.98));
    }

    @Test
    @DisplayName("POST /api/orders/place/{userId} returns 404 when cart is empty")
    void placeOrder_returns404_whenCartEmpty() throws Exception {
        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/orders/place/{userId} returns 403 without token (Spring Security 6 default)")
    void placeOrder_returns403_withoutToken() throws Exception {
        cartItemRepository.save(new CartItem(1, product, alice));

        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/orders/place/{userId} clears the cart after placing the order")
    void placeOrder_clearsCart_afterSuccess() throws Exception {
        cartItemRepository.save(new CartItem(1, product, alice));

        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/orders/place/{userId} decrements product stock on success")
    void placeOrder_decrementsStock() throws Exception {
        cartItemRepository.save(new CartItem(3, product, alice));

        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        // 50 - 3 = 47
        assert updated.getStockQuantity() == 47;
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} returns 200 with list of orders after placing one")
    void getUserOrders_returns200_withOrders() throws Exception {
        // Place an order first so there is something to retrieve
        cartItemRepository.save(new CartItem(1, product, alice));
        mockMvc.perform(post("/api/orders/place/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/user/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Widget"));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} returns empty array when user has no orders")
    void getUserOrders_returnsEmpty_whenNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders/user/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} returns 403 without token (Spring Security 6 default)")
    void getUserOrders_returns403_withoutToken() throws Exception {
        mockMvc.perform(get("/api/orders/user/{userId}", alice.getId()))
                .andExpect(status().isForbidden());
    }
}