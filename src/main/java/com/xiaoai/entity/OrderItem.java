package com.xiaoai.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private Product product; // Optional: To hold product details

    public OrderItem() {
    }

    public OrderItem(int orderId, int productId, int quantity, BigDecimal priceAtPurchase) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
    
    public OrderItem(int orderItemId, int orderId, int productId, int quantity, BigDecimal priceAtPurchase, Product product) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.product = product;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
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
        OrderItem orderItem = (OrderItem) o;
        return orderItemId == orderItem.orderItemId &&
               orderId == orderItem.orderId &&
               productId == orderItem.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId, orderId, productId);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "orderItemId=" + orderItemId +
               ", orderId=" + orderId +
               ", productId=" + productId +
               ", quantity=" + quantity +
               ", priceAtPurchase=" + priceAtPurchase +
               '}';
    }
}
