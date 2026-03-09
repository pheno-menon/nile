package com.saswath.nile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saswath.nile.entity.CartItem;
import com.saswath.nile.entity.Product;
import com.saswath.nile.entity.User;
import com.saswath.nile.repository.CartItemRepository;
import com.saswath.nile.repository.OrderItemRepository;
import com.saswath.nile.repository.OrderRepository;
import com.saswath.nile.repository.ProductRepository;
import com.saswath.nile.repository.UserRepository;
import com.saswath.nile.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CartController — Controller Tests")
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
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

        product = productRepository.save(new Product("Widget", new BigDecimal("9.99"), 10));
    }

    @Test
    @DisplayName("POST /api/cart/add returns 200 and CartItem on valid request")
    void addToCart_returns200_onValidRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", alice.getId(),
                "productId", product.getId(),
                "quantity", 2);

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.product.id").value(product.getId()));
    }

    @Test
    @DisplayName("POST /api/cart/add returns 403 without token (Spring Security 6 default)")
    void addToCart_returns403_withoutToken() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", alice.getId(),
                "productId", product.getId(),
                "quantity", 1);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/cart/add returns 500 when quantity exceeds stock")
    void addToCart_returns500_whenInsufficientStock() throws Exception {
        // product has 10 in stock; requesting 99
        Map<String, Object> body = Map.of(
                "userId", alice.getId(),
                "productId", product.getId(),
                "quantity", 99);

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/cart/add returns 404 when product does not exist")
    void addToCart_returns404_whenProductNotFound() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", alice.getId(),
                "productId", 9999L,
                "quantity", 1);

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cart/{userId} returns 200 with cart items")
    void getCart_returns200_withItems() throws Exception {
        cartItemRepository.save(new CartItem(3, product, alice));

        mockMvc.perform(get("/api/cart/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].quantity").value(3));
    }

    @Test
    @DisplayName("GET /api/cart/{userId} returns empty array when cart is empty")
    void getCart_returnsEmpty_whenCartEmpty() throws Exception {
        mockMvc.perform(get("/api/cart/{userId}", alice.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/cart/{userId} returns 403 without token (Spring Security 6 default)")
    void getCart_returns403_withoutToken() throws Exception {
        mockMvc.perform(get("/api/cart/{userId}", alice.getId()))
                .andExpect(status().isForbidden());
    }
}