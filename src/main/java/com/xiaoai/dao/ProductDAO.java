package com.xiaoai.dao;

import com.xiaoai.entity.Product;
import com.xiaoai.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setStatus(rs.getString("status"));
        product.setCreationDate(rs.getTimestamp("creation_date"));
        product.setViews(rs.getInt("views"));
        return product;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO Products (name, description, price, stock_quantity, category, image_url, status, views, creation_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatus() != null ? product.getStatus() : "上架");
            pstmt.setInt(8, product.getViews() > 0 ? product.getViews() : 0);
            pstmt.setTimestamp(9, product.getCreationDate() != null ? product.getCreationDate() : new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE Products SET name = ?, description = ?, price = ?, stock_quantity = ?, category = ?, image_url = ?, status = ? WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatus());
            pstmt.setInt(8, product.getProductId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public Product getProductById(int productId) {
        String sql = "SELECT * FROM Products WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return null;
    }

    public List<Product> getAllProducts(int pageNumber, int pageSize) {
        String sql = "SELECT * FROM Products ORDER BY creation_date DESC LIMIT ? OFFSET ?";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (pageNumber - 1) * pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return products;
    }

    public int getTotalProductCount() {
        String sql = "SELECT COUNT(*) FROM Products";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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

    public List<Product> getProductsByStatus(String status, int pageNumber, int pageSize) {
        String sql = "SELECT * FROM Products WHERE status = ? ORDER BY creation_date DESC LIMIT ? OFFSET ?";
        List<Product> products = new ArrayList<>();
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
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return products;
    }

    public int getTotalProductCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Products WHERE status = ?";
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
    
    public List<Product> searchProducts(String searchTerm, String category, int pageNumber, int pageSize) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sqlBuilder.append(" AND category LIKE ?"); // Using LIKE for category for flexibility, can be =
            params.add("%" + category + "%");
        }
        sqlBuilder.append(" ORDER BY creation_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNumber - 1) * pageSize);

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return products;
    }

    public int getTotalSearchProductCount(String searchTerm, String category) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM Products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sqlBuilder.append(" AND category LIKE ?");
            params.add("%" + category + "%");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
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

    public boolean updateProductStock(int productId, int newStockQuantity) {
        String sql = "UPDATE Products SET stock_quantity = ? WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newStockQuantity);
            pstmt.setInt(2, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean incrementProductViews(int productId) {
        String sql = "UPDATE Products SET views = views + 1 WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }
    
    public List<Product> getHotProducts(int limit) {
        String sql = "SELECT * FROM Products WHERE status = '热销' ORDER BY views DESC, creation_date DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return products;
    }
}
