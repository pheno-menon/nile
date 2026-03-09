package com.saswath.nile.unit;

import com.saswath.nile.entity.Product;
import com.saswath.nile.exception.ResourceNotFoundException;
import com.saswath.nile.repository.ProductRepository;
import com.saswath.nile.service.ProductService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService — Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("Widget", new BigDecimal("9.99"), 50);
        sampleProduct.setId(1L);
    }

    @Test
    @DisplayName("createProduct saves and returns the product")
    void createProduct_savesAndReturnsProduct() {
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = productService.createProduct(sampleProduct);

        assertThat(result).isEqualTo(sampleProduct);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("getProductById returns product when it exists")
    void getProductById_returnsProduct_whenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById(1L);

        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
    }

    @Test
    @DisplayName("getProductById throws ResourceNotFoundException when product does not exist")
    void getProductById_throws_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("getAllProducts returns all products from repository")
    void getAllProducts_returnsAll() {
        Product p2 = new Product("Gadget", new BigDecimal("19.99"), 20);
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct, p2));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName).containsExactly("Widget", "Gadget");
    }

    @Test
    @DisplayName("getAllProducts returns empty list when no products exist")
    void getAllProducts_returnsEmpty_whenNoneExist() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertThat(productService.getAllProducts()).isEmpty();
    }

    @Test
    @DisplayName("updateProduct applies all field changes and saves")
    void updateProduct_updatesAllFields() {
        Product update = new Product("Updated Widget", new BigDecimal("14.99"), 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(1L, update);

        assertThat(result.getName()).isEqualTo("Updated Widget");
        assertThat(result.getPrice()).isEqualByComparingTo("14.99");
        assertThat(result.getStockQuantity()).isEqualTo(100);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("updateProduct throws ResourceNotFoundException when product does not exist")
    void updateProduct_throws_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, sampleProduct))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteProduct delegates to repository deleteById")
    void deleteProduct_callsRepositoryDelete() {
        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }
}