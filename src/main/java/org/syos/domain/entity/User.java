// File: src/main/java/org/syos/domain/entity/User.java
package org.syos.domain.entity;

import org.syos.domain.enums.UserRole;
import org.syos.domain.valueobject.Password;
import java.util.Objects;

/**
 * Represents system users (Cashier, Manager, Admin).
 */
public class User {
    private Long userId;
    private String username;
    private Password password;
    private String fullName;
    private UserRole role;
    private boolean active;
    
    public User(String username, Password password, String fullName, UserRole role) {
        validateInputs(username, password, fullName, role);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = true;
    }
    
    public User(Long userId, String username, Password password, String fullName, 
                UserRole role, boolean active) {
        this(username, password, fullName, role);
        this.userId = userId;
        this.active = active;
    }
    
    private void validateInputs(String username, Password password, String fullName, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
    }
    
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }
    
    // Getters and setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public void setPassword(Password password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}