package com.saswath.nile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saswath.nile.auth.LoginRequest;
import com.saswath.nile.auth.RegisterRequest;
import com.saswath.nile.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController — Controller Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("register returns 200 with token and user on valid request")
    void register_returns200_onValidRequest() throws Exception {
        RegisterRequest req = new RegisterRequest("Alice", "alice@nile.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("alice@nile.com"))
                .andExpect(jsonPath("$.user.name").value("Alice"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("register returns 400 when email is already taken")
    void register_returns400_whenEmailAlreadyExists() throws Exception {
        // Seed an existing user
        User existing = new User("Alice", "alice@nile.com", passwordEncoder.encode("password123"));
        existing.setRole("ROLE_USER");
        userRepository.save(existing);

        RegisterRequest req = new RegisterRequest("Alice2", "alice@nile.com", "newpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("register does not expose password in response")
    void register_doesNotExposePassword() throws Exception {
        RegisterRequest req = new RegisterRequest("Alice", "alice@nile.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("login returns 200 with token on valid credentials")
    void login_returns200_onValidCredentials() throws Exception {
        User user = new User("Alice", "alice@nile.com", passwordEncoder.encode("password123"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        LoginRequest req = new LoginRequest("alice@nile.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("alice@nile.com"));
    }

    @Test
    @DisplayName("login returns 401 when password is incorrect")
    void login_returns401_onWrongPassword() throws Exception {
        User user = new User("Alice", "alice@nile.com", passwordEncoder.encode("correct"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        LoginRequest req = new LoginRequest("alice@nile.com", "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login returns 500 when email is not registered")
    void login_returns500_whenUserNotFound() throws Exception {
        LoginRequest req = new LoginRequest("ghost@nile.com", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // GlobalExceptionHandler maps unhandled RuntimeException → 500
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("login does not expose password in response")
    void login_doesNotExposePassword() throws Exception {
        User user = new User("Alice", "alice@nile.com", passwordEncoder.encode("password123"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        LoginRequest req = new LoginRequest("alice@nile.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }
}