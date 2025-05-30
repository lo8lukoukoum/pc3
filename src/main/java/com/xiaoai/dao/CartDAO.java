package com.xiaoai.dao;

import com.xiaoai.entity.Cart;
import com.xiaoai.entity.CartItem;
import com.xiaoai.entity.Product;
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

public class CartDAO {

    // --- Helper Methods ---
    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setCartId(rs.getInt("cart_id"));
        cart.setUserId(rs.getInt("user_id"));
        cart.setCreatedAt(rs.getTimestamp("created_at"));
        cart.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cart;
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setCartId(rs.getInt("cart_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setAddedAt(rs.getTimestamp("added_at"));

        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        if (hasColumn(rs, "product_name")) {
            product.setName(rs.getString("product_name"));
        }
        if (hasColumn(rs, "product_price")) {
            product.setPrice(rs.getBigDecimal("product_price"));
        }
        if (hasColumn(rs, "product_image_url")) {
            product.setImageUrl(rs.getString("product_image_url"));
        }
        if (hasColumn(rs, "product_stock")) {
            product.setStockQuantity(rs.getInt("product_stock"));
        }
        item.setProduct(product);
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

    // --- Cart Methods ---

    private int getOrCreateCartIdByUserId(int userId, Connection conn) throws SQLException {
        String selectSql = "SELECT cart_id FROM Cart WHERE user_id = ?";
        String insertSql = "INSERT INTO Cart (user_id, created_at, updated_at) VALUES (?, NOW(), NOW())";
        PreparedStatement pstmtSelect = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;
        int cartId = 0;

        try {
            pstmtSelect = conn.prepareStatement(selectSql);
            pstmtSelect.setInt(1, userId);
            rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                cartId = rs.getInt("cart_id");
            } else {
                // Create cart if not exists
                pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                pstmtInsert.setInt(1, userId);
                pstmtInsert.executeUpdate();
                rs = pstmtInsert.getGeneratedKeys();
                if (rs.next()) {
                    cartId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to create cart for user_id: " + userId);
                }
            }
        } finally {
            // ResultSet and PreparedStatements will be closed by the calling method or its finally block
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstmtSelect != null) try { pstmtSelect.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstmtInsert != null) try { pstmtInsert.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return cartId;
    }
    
    // Public helper if direct cart_id retrieval is needed
    public int getCartIdByUserId(int userId, boolean createIfNotExist) {
        Connection conn = null;
        int cartId = 0;
        try {
            conn = DBConnectionUtil.getConnection();
            if (createIfNotExist) {
                 cartId = getOrCreateCartIdByUserId(userId, conn);
            } else {
                String selectSql = "SELECT cart_id FROM Cart WHERE user_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(selectSql);
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    cartId = rs.getInt("cart_id");
                }
                DBConnectionUtil.closeConnection(null, pstmt, rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0; // Indicate error or not found
        } finally {
            DBConnectionUtil.closeConnection(conn);
        }
        return cartId;
    }


    public Cart getCartByUserId(int userId) {
        Cart cart = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sqlCart = "SELECT * FROM Cart WHERE user_id = ?";

        try {
            conn = DBConnectionUtil.getConnection();
            int cartId = getOrCreateCartIdByUserId(userId, conn); // Ensures cart exists

            pstmt = conn.prepareStatement(sqlCart);
            pstmt.setInt(1, userId); // Re-fetch cart details with updated timestamps if any
            rs = pstmt.executeQuery();

            if (rs.next()) {
                cart = mapResultSetToCart(rs);
                cart.setItems(getCartItems(cart.getCartId(), conn)); // Pass connection
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return cart;
    }

    public boolean addCartItem(int userId, int productId, int quantity) {
        Connection conn = null;
        PreparedStatement pstmtSelect = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;
        boolean success = false;

        String sqlSelectExisting = "SELECT cart_item_id, quantity FROM CartItems WHERE cart_id = ? AND product_id = ?";
        String sqlUpdateQuantity = "UPDATE CartItems SET quantity = ?, added_at = NOW() WHERE cart_item_id = ?";
        String sqlInsertNew = "INSERT INTO CartItems (cart_id, product_id, quantity, added_at) VALUES (?, ?, ?, NOW())";

        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // Manage transaction

            int cartId = getOrCreateCartIdByUserId(userId, conn);
            if (cartId == 0) return false; // Failed to get/create cart

            pstmtSelect = conn.prepareStatement(sqlSelectExisting);
            pstmtSelect.setInt(1, cartId);
            pstmtSelect.setInt(2, productId);
            rs = pstmtSelect.executeQuery();

            if (rs.next()) { // Item exists, update quantity
                int existingItemId = rs.getInt("cart_item_id");
                int existingQuantity = rs.getInt("quantity");
                pstmtUpdate = conn.prepareStatement(sqlUpdateQuantity);
                pstmtUpdate.setInt(1, existingQuantity + quantity);
                pstmtUpdate.setInt(2, existingItemId);
                success = pstmtUpdate.executeUpdate() > 0;
            } else { // Item does not exist, insert new
                pstmtInsert = conn.prepareStatement(sqlInsertNew);
                pstmtInsert.setInt(1, cartId);
                pstmtInsert.setInt(2, productId);
                pstmtInsert.setInt(3, quantity);
                success = pstmtInsert.executeUpdate() > 0;
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            success = false;
        } finally {
            DBConnectionUtil.closeConnection(null, pstmtSelect, rs); // rs closed with pstmtSelect
            DBConnectionUtil.closeConnection(null, pstmtUpdate);
            DBConnectionUtil.closeConnection(null, pstmtInsert);
            if (conn != null) try { conn.setAutoCommit(true); DBConnectionUtil.closeConnection(conn); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

    public boolean updateCartItemQuantity(int userId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeCartItem(userId, productId);
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        String sql = "UPDATE CartItems SET quantity = ?, added_at = NOW() WHERE cart_id = ? AND product_id = ?";
        try {
            conn = DBConnectionUtil.getConnection();
            int cartId = getCartIdByUserId(userId, false); // Don't create if no cart
            if (cartId == 0) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, cartId);
            pstmt.setInt(3, productId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
        return success;
    }

    public boolean removeCartItem(int userId, int productId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        String sql = "DELETE FROM CartItems WHERE cart_id = ? AND product_id = ?";
        try {
            conn = DBConnectionUtil.getConnection();
            int cartId = getCartIdByUserId(userId, false); // Don't create if no cart
             if (cartId == 0) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
        return success;
    }

    // Internal version, reuses connection
    public List<CartItem> getCartItems(int cartId, Connection conn) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, p.name as product_name, p.price as product_price, p.image_url as product_image_url, p.stock_quantity as product_stock " +
                     "FROM CartItems ci JOIN Products p ON ci.product_id = p.product_id WHERE ci.cart_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean newConnection = false;

        try {
             if (conn == null || conn.isClosed()) {
                conn = DBConnectionUtil.getConnection();
                newConnection = true;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToCartItem(rs));
            }
        } finally {
             if (newConnection) { // If this method created the connection, it should close everything
                DBConnectionUtil.closeConnection(conn, pstmt, rs);
            } else { // If connection was passed, only close pstmt and rs
                DBConnectionUtil.closeConnection(null, pstmt, rs);
            }
        }
        return items;
    }

    // Public version, handles its own connection
    public List<CartItem> getCartItems(int cartId) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            return getCartItems(cartId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            DBConnectionUtil.closeConnection(conn);
        }
    }


    public boolean clearCart(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        String sql = "DELETE FROM CartItems WHERE cart_id = ?";
        try {
            conn = DBConnectionUtil.getConnection();
            int cartId = getCartIdByUserId(userId, false); // Don't create if no cart
            if (cartId == 0) return true; // No cart or cart already empty

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            pstmt.executeUpdate(); // No need to check rows affected, if cartId is valid, it's cleared.
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
        return success;
    }

    public int getCartItemCount(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT SUM(quantity) FROM CartItems WHERE cart_id = ?";
        try {
            conn = DBConnectionUtil.getConnection();
            int cartId = getCartIdByUserId(userId, false);
            if (cartId == 0) return 0; // No cart

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return count;
    }
}
