package com.saswath.nile.integration;

import com.saswath.nile.entity.CartItem;
import com.saswath.nile.entity.Product;
import com.saswath.nile.entity.User;
import com.saswath.nile.repository.CartItemRepository;
import com.saswath.nile.repository.ProductRepository;
import com.saswath.nile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartItemRepository — Integration Tests")
class CartRepositoryTest {

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    private User alice;
    private User bob;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        alice = new User("Alice", "alice@nile.com", "encoded");
        alice.setRole("ROLE_USER");
        alice = userRepository.save(alice);

        bob = new User("Bob", "bob@nile.com", "encoded");
        bob.setRole("ROLE_USER");
        bob = userRepository.save(bob);

        widget = productRepository.save(new Product("Widget", new BigDecimal("9.99"), 50));
        gadget = productRepository.save(new Product("Gadget", new BigDecimal("19.99"), 20));
    }

    @Test
    @DisplayName("findByUserId returns all cart items belonging to the user")
    void findByUserId_returnsItemsForUser() {
        cartItemRepository.save(new CartItem(1, widget, alice));
        cartItemRepository.save(new CartItem(2, gadget, alice));

        List<CartItem> items = cartItemRepository.findByUserId(alice.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ci -> ci.getProduct().getName())
                .containsExactlyInAnyOrder("Widget", "Gadget");
    }

    @Test
    @DisplayName("findByUserId does not return items belonging to other users")
    void findByUserId_doesNotReturnOtherUsersItems() {
        cartItemRepository.save(new CartItem(1, widget, alice));
        cartItemRepository.save(new CartItem(3, gadget, bob));

        List<CartItem> aliceItems = cartItemRepository.findByUserId(alice.getId());

        assertThat(aliceItems).hasSize(1);
        assertThat(aliceItems.get(0).getProduct().getName()).isEqualTo("Widget");
    }

    @Test
    @DisplayName("findByUserId returns empty list when user has no items in cart")
    void findByUserId_returnsEmpty_whenCartIsEmpty() {
        List<CartItem> items = cartItemRepository.findByUserId(alice.getId());

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndProductId returns the matching cart item")
    void findByUserIdAndProductId_returnsItem_whenFound() {
        CartItem saved = cartItemRepository.save(new CartItem(1, widget, alice));

        Optional<CartItem> result = cartItemRepository
                .findByUserIdAndProductId(alice.getId(), widget.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("findByUserIdAndProductId returns empty when no match")
    void findByUserIdAndProductId_returnsEmpty_whenNotFound() {
        Optional<CartItem> result = cartItemRepository
                .findByUserIdAndProductId(alice.getId(), gadget.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteAll clears all provided cart items")
    void deleteAll_removesCartItems() {
        CartItem item1 = cartItemRepository.save(new CartItem(1, widget, alice));
        CartItem item2 = cartItemRepository.save(new CartItem(2, gadget, alice));

        cartItemRepository.deleteAll(List.of(item1, item2));

        assertThat(cartItemRepository.findByUserId(alice.getId())).isEmpty();
    }
}