package com.xiaoai.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String email;
    private Timestamp registrationDate;
    private String personalSignature;
    private Timestamp lastLogin;

    public User() {
    }

    public User(String username, String password, String role, String email, String personalSignature) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.personalSignature = personalSignature;
    }
    
    public User(int userId, String username, String password, String role, String email, Timestamp registrationDate, String personalSignature, Timestamp lastLogin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.registrationDate = registrationDate;
        this.personalSignature = personalSignature;
        this.lastLogin = lastLogin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    @Override
    public String toString() {
        return "User{" +
               "userId=" + userId +
               ", username='" + username + '\'' +
               ", role='" + role + '\'' +
               ", email='" + email + '\'' +
               ", registrationDate=" + registrationDate +
               ", personalSignature='" + personalSignature + '\'' +
               ", lastLogin=" + lastLogin +
               '}';
    }
}
