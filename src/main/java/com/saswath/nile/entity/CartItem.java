package com.saswath.nile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public CartItem() {}

    public CartItem(Integer quantity, Product product, User user) {
        this.user = user;
        this.quantity = quantity;
        this.product = product;
    }

    public User getUser() { return user; }

    public Long getId() { return id; }

    public Integer getQuantity() { return quantity; }

    public Product getProduct() { return product; }

    public void setUser(User user) { this.user = user; }

    public void setId(Long id) { this.id = id; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public void setProduct(Product product) { this.product = product; }
}
