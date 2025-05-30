package com.xiaoai.dao;

import com.xiaoai.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; // To maintain order for some results if needed
import java.util.List;
import java.util.Map;

public class StatsDAO {

    public BigDecimal getTotalSalesAmount() {
        String sql = "SELECT SUM(total_amount) FROM Orders WHERE status = '已完成'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BigDecimal totalSales = BigDecimal.ZERO;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                if (sum != null) {
                    totalSales = sum;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return totalSales;
    }

    public BigDecimal getSalesAmountLastNDays(int days) {
        String sql = "SELECT SUM(total_amount) FROM Orders WHERE status = '已完成' AND order_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BigDecimal totalSales = BigDecimal.ZERO;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                if (sum != null) {
                    totalSales = sum;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return totalSales;
    }

    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        String sql = "SELECT p.product_id, p.name, SUM(oi.quantity) as total_quantity_sold, " +
                     "SUM(oi.quantity * oi.price_at_purchase) as total_revenue " +
                     "FROM OrderItems oi " +
                     "JOIN Products p ON oi.product_id = p.product_id " +
                     "JOIN Orders o ON oi.order_id = o.order_id " +
                     "WHERE o.status = '已完成' " +
                     "GROUP BY p.product_id, p.name ORDER BY total_quantity_sold DESC LIMIT ?";
        List<Map<String, Object>> topProducts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> productData = new LinkedHashMap<>(); // Use LinkedHashMap to maintain column order
                productData.put("product_id", rs.getInt("product_id"));
                productData.put("name", rs.getString("name"));
                productData.put("total_quantity_sold", rs.getInt("total_quantity_sold"));
                productData.put("total_revenue", rs.getBigDecimal("total_revenue"));
                topProducts.add(productData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return topProducts;
    }

    public List<Map<String, Object>> getTopViewedProducts(int limit) {
        String sql = "SELECT product_id, name, views FROM Products ORDER BY views DESC LIMIT ?";
        List<Map<String, Object>> topProducts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> productData = new LinkedHashMap<>();
                productData.put("product_id", rs.getInt("product_id"));
                productData.put("name", rs.getString("name"));
                productData.put("views", rs.getInt("views"));
                topProducts.add(productData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return topProducts;
    }

    public int getNewCustomersLastNDays(int days) {
        String sql = "SELECT COUNT(*) FROM Users WHERE registration_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
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

    public Map<String, Integer> getOrderCountByStatus() {
        String sql = "SELECT status, COUNT(*) as count FROM Orders GROUP BY status";
        Map<String, Integer> orderCounts = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                orderCounts.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return orderCounts;
    }
}
