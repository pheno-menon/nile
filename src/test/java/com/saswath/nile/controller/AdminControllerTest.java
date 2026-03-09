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
@DisplayName("AdminController — Controller Tests")
class AdminControllerTest {

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
    private User regularUser;
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

        regularUser = new User("Alice", "alice@nile.com", passwordEncoder.encode("pass"));
        regularUser.setRole("ROLE_USER");
        regularUser = userRepository.save(regularUser);
        userToken = "Bearer " + jwtUtil.generateToken("alice@nile.com");

        User admin = new User("Admin", "admin@nile.com", passwordEncoder.encode("pass"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        adminToken = "Bearer " + jwtUtil.generateToken("admin@nile.com");
    }

    @Test
    @DisplayName("POST /api/admin/products returns 200 when called by ADMIN")
    void createProduct_returns200_forAdmin() throws Exception {
        Map<String, Object> body = Map.of("name", "New Product", "price", 15.00, "stock", 30);

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.stockQuantity").value(30));
    }

    @Test
    @DisplayName("POST /api/admin/products returns 403 when called by ROLE_USER")
    void createProduct_returns403_forRegularUser() throws Exception {
        Map<String, Object> body = Map.of("name", "New Product", "price", 15.00, "stock", 30);

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/products returns 403 without token (Spring Security 6 default)")
    void createProduct_returns403_withoutToken() throws Exception {
        Map<String, Object> body = Map.of("name", "New Product", "price", 15.00, "stock", 30);

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/products/{id} returns 200 and updates product for ADMIN")
    void updateProduct_returns200_forAdmin() throws Exception {
        Map<String, Object> body = Map.of("name", "Updated Widget", "price", 12.99, "stock", 75);

        mockMvc.perform(put("/api/admin/products/{id}", savedProduct.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Widget"))
                .andExpect(jsonPath("$.price").value(12.99))
                .andExpect(jsonPath("$.stockQuantity").value(75));
    }

    @Test
    @DisplayName("PUT /api/admin/products/{id} returns 403 for ROLE_USER")
    void updateProduct_returns403_forRegularUser() throws Exception {
        Map<String, Object> body = Map.of("name", "X", "price", 1.00, "stock", 1);

        mockMvc.perform(put("/api/admin/products/{id}", savedProduct.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/admin/products/{id} returns 204 for ADMIN")
    void deleteProduct_returns204_forAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/products/{id}", savedProduct.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/products/{id} returns 403 for ROLE_USER")
    void deleteProduct_returns403_forRegularUser() throws Exception {
        mockMvc.perform(delete("/api/admin/products/{id}", savedProduct.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users returns 200 with user list for ADMIN")
    void getAllUsers_returns200_forAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").exists());
    }

    @Test
    @DisplayName("GET /api/admin/users returns 403 for ROLE_USER")
    void getAllUsers_returns403_forRegularUser() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users returns 403 without token (Spring Security 6 default)")
    void getAllUsers_returns403_withoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 204 for ADMIN")
    void deleteUser_returns204_forAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}", regularUser.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 403 for ROLE_USER")
    void deleteUser_returns403_forRegularUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}", regularUser.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 404 when user does not exist")
    void deleteUser_returns404_whenUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}", 9999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}