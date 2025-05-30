package com.xiaoai.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class Message {
    private int messageId;
    private Integer userId; // Can be null for guest messages
    private String name;    // For guests
    private String email;   // For guests
    private String messageText;
    private Timestamp messageDate;
    private String status;

    public Message() {
    }

    public Message(Integer userId, String name, String email, String messageText, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.messageText = messageText;
        this.status = status;
    }
    
    public Message(int messageId, Integer userId, String name, String email, String messageText, Timestamp messageDate, String status) {
        this.messageId = messageId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.messageText = messageText;
        this.messageDate = messageDate;
        this.status = status;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Timestamp messageDate) {
        this.messageDate = messageDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageId == message.messageId &&
               Objects.equals(userId, message.userId) &&
               Objects.equals(messageDate, message.messageDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, userId, messageDate);
    }

    @Override
    public String toString() {
        return "Message{" +
               "messageId=" + messageId +
               ", userId=" + userId +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", messageText='" + messageText + '\'' +
               ", messageDate=" + messageDate +
               ", status='" + status + '\'' +
               '}';
    }
}
