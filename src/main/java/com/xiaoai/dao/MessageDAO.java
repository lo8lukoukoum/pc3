package com.xiaoai.dao;

import com.xiaoai.entity.Message;
// User import is not strictly needed if we only fetch username as a string and don't map to User entity directly in Message
// import com.xiaoai.entity.User; 
import com.xiaoai.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types; // For setting NULL for Integer userId
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        
        // Handle nullable user_id
        int userId = rs.getInt("user_id");
        if (rs.wasNull()) {
            message.setUserId(null);
        } else {
            message.setUserId(userId);
        }
        
        message.setName(rs.getString("name"));
        message.setEmail(rs.getString("email"));
        message.setMessageText(rs.getString("message_text"));
        message.setMessageDate(rs.getTimestamp("message_date"));
        message.setStatus(rs.getString("status"));

        // Optionally, if a username is joined and needed (e.g., for display in a DTO that combines Message and username)
        // The Message entity itself does not have a username field.
        // if (hasColumn(rs, "user_username")) {
        //     String username = rs.getString("user_username");
        //     // message.setAssociatedUsername(username); // If Message entity had such a field
        // }
        return message;
    }
    
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addMessage(Message message) {
        String sql = "INSERT INTO Messages (user_id, name, email, message_text, status, message_date) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            if (message.getUserId() != null) {
                pstmt.setInt(1, message.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, message.getName());
            pstmt.setString(3, message.getEmail());
            pstmt.setString(4, message.getMessageText());
            pstmt.setString(5, message.getStatus() != null ? message.getStatus() : "待审核");
            pstmt.setTimestamp(6, message.getMessageDate() != null ? message.getMessageDate() : new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, u.username as user_username FROM Messages m LEFT JOIN Users u ON m.user_id = u.user_id WHERE m.message_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return null;
    }

    public List<Message> getMessagesByStatus(String status, int pageNumber, int pageSize) {
        String sql = "SELECT m.*, u.username as user_username FROM Messages m LEFT JOIN Users u ON m.user_id = u.user_id " +
                     "WHERE m.status = ? ORDER BY m.message_date DESC LIMIT ? OFFSET ?";
        List<Message> messages = new ArrayList<>();
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
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return messages;
    }

    public int getTotalMessageCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Messages WHERE status = ?";
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

    public boolean updateMessageStatus(int messageId, String newStatus) {
        String sql = "UPDATE Messages SET status = ? WHERE message_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, messageId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM Messages WHERE message_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt);
        }
    }

    public List<Message> getAllMessages(int pageNumber, int pageSize) {
        String sql = "SELECT m.*, u.username as user_username FROM Messages m LEFT JOIN Users u ON m.user_id = u.user_id " +
                     "ORDER BY m.message_date DESC LIMIT ? OFFSET ?";
        List<Message> messages = new ArrayList<>();
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
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, pstmt, rs);
        }
        return messages;
    }

    public int getTotalMessageCount() {
        String sql = "SELECT COUNT(*) FROM Messages";
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
}
