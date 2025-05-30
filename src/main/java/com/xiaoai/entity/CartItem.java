package com.xiaoai.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
    private Timestamp addedAt;
    private Product product; // Optional: To hold product details

    public CartItem() {
    }

    public CartItem(int cartId, int productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public CartItem(int cartItemId, int cartId, int productId, int quantity, Timestamp addedAt, Product product) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
        this.product = product;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return cartItemId == cartItem.cartItemId &&
               cartId == cartItem.cartId &&
               productId == cartItem.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemId, cartId, productId);
    }

    @Override
    public String toString() {
        return "CartItem{" +
               "cartItemId=" + cartItemId +
               ", cartId=" + cartId +
               ", productId=" + productId +
               ", quantity=" + quantity +
               ", addedAt=" + addedAt +
               '}';
    }
}
