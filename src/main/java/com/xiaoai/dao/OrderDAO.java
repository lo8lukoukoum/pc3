package com.xiaoai.dao;

import com.xiaoai.entity.Order;
import com.xiaoai.entity.OrderItem;
import com.xiaoai.entity.Product; // Required for populating product details in OrderItem
import com.xiaoai.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // --- Helper Methods ---
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        return order;
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPriceAtPurchase(rs.getBigDecimal("price_at_purchase"));

        // Optionally populate basic product info if joined in query
        if (hasColumn(rs, "product_name")) {
            Product product = new Product();
            product.setProductId(rs.getInt("product_id")); // Redundant but good for consistency
            product.setName(rs.getString("product_name"));
            if (hasColumn(rs, "product_image_url")) {
                product.setImageUrl(rs.getString("product_image_url"));
            }
            item.setProduct(product);
        }
        return item;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    // --- Order Methods ---

    public int createOrder(Order order) {
        String sqlOrder = "INSERT INTO Orders (user_id, total_amount, status, shipping_address, order_date) VALUES (?, ?, ?, ?, ?)";
        String sqlOrderItem = "INSERT INTO OrderItems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";

        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtOrderItem = null;
        PreparedStatement pstmtUpdateStock = null;
        ResultSet generatedKeys = null;
        int orderId = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert Order
            pstmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getUserId());
            pstmtOrder.setBigDecimal(2, order.getTotalAmount());
            pstmtOrder.setString(3, order.getStatus());
            pstmtOrder.setString(4, order.getShippingAddress());
            pstmtOrder.setTimestamp(5, order.getOrderDate() != null ? order.getOrderDate() : new Timestamp(System.currentTimeMillis()));
            pstmtOrder.executeUpdate();

            generatedKeys = pstmtOrder.getGeneratedKeys();
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // Insert OrderItems and Update Stock
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                pstmtOrderItem = conn.prepareStatement(sqlOrderItem);
                pstmtUpdateStock = conn.prepareStatement(sqlUpdateStock);

                for (OrderItem item : order.getItems()) {
                    // Check stock before attempting update
                    ProductDAO tempProductDAO = new ProductDAO(); // Temporary, or pass ProductDAO instance
                    Product product = tempProductDAO.getProductById(item.getProductId()); // Fetch current product state
                    if (product == null || product.getStockQuantity() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for product ID: " + item.getProductId());
                    }

                    // Insert OrderItem
                    item.setOrderId(orderId); // Set the generated orderId
                    pstmtOrderItem.setInt(1, item.getOrderId());
                    pstmtOrderItem.setInt(2, item.getProductId());
                    pstmtOrderItem.setInt(3, item.getQuantity());
                    pstmtOrderItem.setBigDecimal(4, item.getPriceAtPurchase());
                    pstmtOrderItem.addBatch();

                    // Prepare stock update
                    pstmtUpdateStock.setInt(1, item.getQuantity());
                    pstmtUpdateStock.setInt(2, item.getProductId());
                    pstmtUpdateStock.setInt(3, item.getQuantity()); // Ensure stock_quantity >= item.getQuantity()
                    pstmtUpdateStock.addBatch();
                }
                pstmtOrderItem.executeBatch();
                
                // Execute stock updates
                int[] stockUpdateResults = pstmtUpdateStock.executeBatch();
                for (int result : stockUpdateResults) {
                    if (result == 0) { // Check if any stock update failed (e.g. due to concurrent modification or insufficient stock)
                        throw new SQLException("Stock update failed for one or more items. Order rolled back.");
                    }
                }
            }
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            orderId = -1; // Indicate failure
        } finally {
            DBConnectionUtil.closeConnection(null, pstmtUpdateStock); // Close this first
            DBConnectionUtil.closeConnection(null, pstmtOrderItem);  // Then this
            DBConnectionUtil.closeConnection(conn, pstmtOrder, generatedKeys); // Connection closed here
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default auto-commit behavior
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return orderId;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        Order order = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                order = mapResultSetToOrder(rs);
                // Fetch and set order items
                order.setItems(getOrderItemsByOrderId(orderId, conn)); // Pass connection to reuse
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return order;
    }
    
    public List<Order> getOrdersByUserId(int userId, int pageNumber, int pageSize) {
        String sql = "SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC LIMIT ? OFFSET ?";
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (pageNumber - 1) * pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Optionally, fetch items here or leave it to a separate call for performance
                // order.setItems(getOrderItemsByOrderId(order.getOrderId(), conn)); 
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return orders;
    }

    public int getTotalOrderCountByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM Orders WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return 0;
    }

    public List<Order> getOrdersByStatus(String status, int pageNumber, int pageSize) {
        String sql = "SELECT * FROM Orders WHERE status = ? ORDER BY order_date DESC LIMIT ? OFFSET ?";
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (pageNumber - 1) * pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                 Order order = mapResultSetToOrder(rs);
                // order.setItems(getOrderItemsByOrderId(order.getOrderId(), conn)); // Optional
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return orders;
    }

    public int getTotalOrderCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Orders WHERE status = ?";
         Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return 0;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    /**
     * Deletes an order. OrderItems are deleted by cascade. Stock is NOT restored.
     */
    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        // Add transaction management if stock restoration was needed
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    // --- OrderItem Methods ---

    // Overloaded version for internal use when connection is already available
    public List<OrderItem> getOrderItemsByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "SELECT oi.*, p.name as product_name, p.image_url as product_image_url " +
                     "FROM OrderItems oi JOIN Products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean newConnection = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DBConnectionUtil.getConnection();
                newConnection = true;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToOrderItem(rs));
            }
        } finally {
            // Only close resources if a new connection was made in this method call
            if (newConnection) {
                DBConnectionUtil.closeConnection(conn, pstmt, rs);
            } else {
                 // Only close pstmt and rs, leave conn open for the caller
                DBConnectionUtil.closeConnection(null, pstmt, rs);
            }
        }
        return items;
    }
    
    // Public version that handles its own connection
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            return getOrderItemsByOrderId(orderId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        } finally {
            DBConnectionUtil.closeConnection(conn); // Close the connection if it was opened here
        }
    }
}
