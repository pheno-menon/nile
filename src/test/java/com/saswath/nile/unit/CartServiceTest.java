package com.saswath.nile.unit;

import com.saswath.nile.entity.CartItem;
import com.saswath.nile.entity.Product;
import com.saswath.nile.entity.User;
import com.saswath.nile.exception.ResourceNotFoundException;
import com.saswath.nile.repository.CartItemRepository;
import com.saswath.nile.repository.ProductRepository;
import com.saswath.nile.repository.UserRepository;
import com.saswath.nile.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService — Unit Tests")
class CartServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@nile.com", "encoded");
        user.setId(1L);
        user.setRole("ROLE_USER");

        product = new Product("Widget", new BigDecimal("9.99"), 10);
        product.setId(1L);
    }

    @Test
    @DisplayName("addToCart saves and returns CartItem when stock is sufficient")
    void addToCart_succeeds_whenStockIsSufficient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        CartItem result = cartService.addToCart(1L, 1L, 3);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getProduct()).isEqualTo(product);
        assertThat(result.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart throws RuntimeException when quantity exceeds stock")
    void addToCart_throws_whenInsufficientStock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // product has 10 in stock; requesting 11
        assertThatThrownBy(() -> cartService.addToCart(1L, 1L, 11))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart throws ResourceNotFoundException when user does not exist")
    void addToCart_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(99L, 1L, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("addToCart throws ResourceNotFoundException when product does not exist")
    void addToCart_throws_whenProductNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L, 99L, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("addToCart succeeds when quantity exactly equals stock")
    void addToCart_succeedsWhenQuantityEqualsStock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        // product has exactly 10 in stock; requesting exactly 10
        CartItem result = cartService.addToCart(1L, 1L, 10);
        assertThat(result.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("getUserCart returns all cart items for the user")
    void getUserCart_returnsAllItems() {
        CartItem item = new CartItem(2, product, user);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(item));

        List<CartItem> result = cartService.getUserCart(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct()).isEqualTo(product);
    }

    @Test
    @DisplayName("getUserCart returns empty list when cart is empty")
    void getUserCart_returnsEmpty_whenCartIsEmpty() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThat(cartService.getUserCart(1L)).isEmpty();
    }

    @Test
    @DisplayName("removeFromCart delegates to repository deleteById")
    void removeFromCart_callsRepositoryDelete() {
        cartService.removeFromCart(5L);

        verify(cartItemRepository).deleteById(5L);
    }
}