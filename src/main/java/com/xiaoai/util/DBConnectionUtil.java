package com.xiaoai.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnectionUtil {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/xiao_ai_trade?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Default for local setup

    // Static block to load the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            // Consider throwing a runtime exception if the application cannot proceed without the driver
            // For example: throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A Connection object or null if an error occurs.
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Closes the database connection, PreparedStatement, and ResultSet.
     *
     * @param conn  The Connection to close.
     * @param pstmt The PreparedStatement to close.
     * @param rs    The ResultSet to close.
     */
    public static void closeConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            if (pstmt != null && !pstmt.isClosed()) {
                pstmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing PreparedStatement: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection and PreparedStatement.
     *
     * @param conn  The Connection to close.
     * @param pstmt The PreparedStatement to close.
     */
    public static void closeConnection(Connection conn, PreparedStatement pstmt) {
        closeConnection(conn, pstmt, null);
    }

    /**
     * Closes the database connection.
     *
     * @param conn The Connection to close.
     */
    public static void closeConnection(Connection conn) {
        closeConnection(conn, null, null);
    }

    // Optional: Main method for quick connection testing
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            if (conn != null) {
                System.out.println("Successfully connected to the database!");
                // You can perform a simple query here for further testing if needed
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                closeConnection(conn);
                System.out.println("Database connection closed.");
            }
        }
    }
}
