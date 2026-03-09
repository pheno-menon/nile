package com.saswath.nile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("ProductController — Controller Tests")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private Product savedProduct;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Delete in FK-safe order: child tables before parent tables
        cartItemRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        savedProduct = productRepository.save(
                new Product("Widget", new BigDecimal("9.99"), 50));

        User user = new User("Alice", "alice@nile.com", passwordEncoder.encode("pass"));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        userToken = "Bearer " + jwtUtil.generateToken("alice@nile.com");

        User admin = new User("Admin", "admin@nile.com", passwordEncoder.encode("pass"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        adminToken = "Bearer " + jwtUtil.generateToken("admin@nile.com");
    }

    @Test
    @DisplayName("GET /api/products returns 200 without authentication")
    void getAllProducts_returns200_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/products returns the saved product in the list")
    void getAllProducts_returnsSavedProduct() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Widget"))
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 200 for existing product without auth")
    void getProductById_returns200_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 for non-existent product")
    void getProductById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 9999L)
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products returns 403 without token (Spring Security 6 default)")
    void createProduct_returns403_withoutToken() throws Exception {
        Map<String, Object> body = Map.of("name", "New Product", "price", 5.00, "stock", 10);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/products returns 403 when authenticated as ROLE_USER")
    void createProduct_returns403_forRegularUser() throws Exception {
        Map<String, Object> body = Map.of("name", "New Product", "price", 5.00, "stock", 10);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} returns 403 without token (Spring Security 6 default)")
    void deleteProduct_returns403_withoutToken() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} returns 403 when authenticated as ROLE_USER")
    void deleteProduct_returns403_forRegularUser() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }
}