package com.xiaoai.dao;

import com.xiaoai.entity.Review;
import com.xiaoai.entity.User; // For populating user info in Review
import com.xiaoai.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setCommentText(rs.getString("comment_text"));
        review.setImageUrl(rs.getString("image_url"));
        review.setVideoUrl(rs.getString("video_url"));
        review.setAnonymous(rs.getBoolean("is_anonymous"));
        review.setReviewDate(rs.getTimestamp("review_date"));
        review.setStatus(rs.getString("status"));

        // If username is joined, populate it (e.g., for display)
        // This requires the calling query to alias Users.username as user_username
        if (hasColumn(rs, "user_username")) {
            // We don't have a full User object in Review entity, but can store username if needed
            // For simplicity, we assume Review entity might have a transient field for username or it's handled in a DTO.
            // Here, we'll just show how it could be fetched. The Review entity itself doesn't store a User object.
            // String username = rs.getString("user_username"); 
        }
        return review;
    }
    
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addReview(Review review) {
        String sql = "INSERT INTO Reviews (product_id, user_id, rating, comment_text, image_url, video_url, is_anonymous, status, review_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, review.getProductId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setInt(3, review.getRating()); // Assuming 0 if not applicable
            pstmt.setString(4, review.getCommentText());
            pstmt.setString(5, review.getImageUrl());
            pstmt.setString(6, review.getVideoUrl());
            pstmt.setBoolean(7, review.isAnonymous());
            pstmt.setString(8, review.getStatus() != null ? review.getStatus() : "待审核");
            pstmt.setTimestamp(9, review.getReviewDate() != null ? review.getReviewDate() : new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public Review getReviewById(int reviewId) {
        // The query joins with Users table but mapResultSetToReview doesn't store the User object directly in Review.
        // If a User object is needed in Review, the Review entity should be modified.
        String sql = "SELECT r.*, u.username as user_username FROM Reviews r JOIN Users u ON r.user_id = u.user_id WHERE r.review_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reviewId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToReview(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return null;
    }

    public List<Review> getReviewsByProductId(int productId, int pageNumber, int pageSize) {
        String sql = "SELECT r.*, u.username as user_username FROM Reviews r JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? AND r.status = '已批准' ORDER BY r.review_date DESC LIMIT ? OFFSET ?";
        List<Review> reviews = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (pageNumber - 1) * pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return reviews;
    }

    public int getTotalReviewCountByProductId(int productId) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE product_id = ? AND status = '已批准'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
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

    public List<Review> getReviewsByUserId(int userId, int pageNumber, int pageSize) {
        String sql = "SELECT r.*, u.username as user_username FROM Reviews r JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? ORDER BY r.review_date DESC LIMIT ? OFFSET ?";
        List<Review> reviews = new ArrayList<>();
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
                reviews.add(mapResultSetToReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return reviews;
    }
    
    public int getTotalReviewCountByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE user_id = ?"; // Note: SQL in description had 'r.user_id' which is fine
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

    public List<Review> getReviewsByStatus(String status, int pageNumber, int pageSize) {
        String sql = "SELECT r.*, u.username as user_username FROM Reviews r JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.status = ? ORDER BY r.review_date DESC LIMIT ? OFFSET ?";
        List<Review> reviews = new ArrayList<>();
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
                reviews.add(mapResultSetToReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return reviews;
    }

    public int getTotalReviewCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE status = ?";
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

    public boolean updateReviewStatus(int reviewId, String newStatus) {
        String sql = "UPDATE Reviews SET status = ? WHERE review_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reviewId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM Reviews WHERE review_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reviewId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean checkIfUserCanReviewProduct(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM Orders o " +
                     "JOIN OrderItems oi ON o.order_id = oi.order_id " +
                     "WHERE o.user_id = ? AND oi.product_id = ? AND o.status = '已完成'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return false;
    }
}
