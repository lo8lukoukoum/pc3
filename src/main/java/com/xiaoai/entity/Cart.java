package com.xiaoai.entity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class Cart {
    private int cartId;
    private int userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<CartItem> items;

    public Cart() {
    }

    public Cart(int userId) {
        this.userId = userId;
    }

    public Cart(int cartId, int userId, Timestamp createdAt, Timestamp updatedAt, List<CartItem> items) {
        this.cartId = cartId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return cartId == cart.cartId &&
               userId == cart.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, userId);
    }

    @Override
    public String toString() {
        return "Cart{" +
               "cartId=" + cartId +
               ", userId=" + userId +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
