package com.saswath.nile.service;

import com.saswath.nile.entity.Product;
import com.saswath.nile.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product updateProduct(Long id, Product updateProduct) {
        Product product = getProductById(id);

        product.setName(updateProduct.getName());
        product.setPrice(updateProduct.getPrice());
        product.setStockQuantity(updateProduct.getStockQuantity());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
