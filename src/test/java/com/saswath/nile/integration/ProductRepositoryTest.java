package com.saswath.nile.integration;

import com.saswath.nile.entity.Product;
import com.saswath.nile.repository.ProductRepository;
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
@DisplayName("ProductRepository — Integration Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        widget = productRepository.save(new Product("Widget Pro", new BigDecimal("9.99"), 50));
        gadget = productRepository.save(new Product("Gadget Plus", new BigDecimal("29.99"), 0));
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase returns matching products")
    void findByNameContaining_returnsMatches() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("widget");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Widget Pro");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase is case-insensitive")
    void findByNameContaining_isCaseInsensitive() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("GADGET");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Gadget Plus");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase returns multiple results for partial match")
    void findByNameContaining_returnsMultiple_forPartialMatch() {
        // Both "Widget Pro" and "Gadget Plus" contain "et" - save a third product to broaden
        productRepository.save(new Product("Net Tool", new BigDecimal("5.00"), 10));

        List<Product> results = productRepository.findByNameContainingIgnoreCase("et");
        // "Widget" contains "et" → matches; "Gadget" contains "et" → matches; "Net" contains "et" → matches
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase returns empty list when no products match")
    void findByNameContaining_returnsEmpty_whenNoMatch() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("zzznomatch");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all saved products")
    void findAll_returnsAllProducts() {
        List<Product> all = productRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget Pro", "Gadget Plus");
    }

    @Test
    @DisplayName("findById returns product when it exists")
    void findById_returnsProduct_whenExists() {
        Optional<Product> found = productRepository.findById(widget.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualByComparingTo("9.99");
    }

    @Test
    @DisplayName("findById returns empty when product does not exist")
    void findById_returnsEmpty_whenNotFound() {
        assertThat(productRepository.findById(9999L)).isEmpty();
    }

    @Test
    @DisplayName("save persists stock quantity correctly")
    void save_persistsStockQuantity() {
        Product p = productRepository.findById(gadget.getId()).orElseThrow();
        assertThat(p.getStockQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("update changes product fields in place")
    void update_changesFields() {
        widget.setPrice(new BigDecimal("14.99"));
        widget.setStockQuantity(100);
        productRepository.save(widget);

        Product updated = productRepository.findById(widget.getId()).orElseThrow();
        assertThat(updated.getPrice()).isEqualByComparingTo("14.99");
        assertThat(updated.getStockQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("delete removes product from database")
    void delete_removesProduct() {
        productRepository.deleteById(widget.getId());

        assertThat(productRepository.findById(widget.getId())).isEmpty();
        assertThat(productRepository.findAll()).hasSize(1);
    }
}