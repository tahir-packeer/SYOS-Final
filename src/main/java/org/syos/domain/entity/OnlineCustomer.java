// File: src/main/java/org/syos/domain/entity/OnlineCustomer.java
package org.syos.domain.entity;

import org.syos.domain.valueobject.Password;
import java.util.Objects;

/**
 * Represents an online customer (website sales).
 * Separate entity with different requirements from normal customers.
 */
public class OnlineCustomer {
    private Long onlineCustomerId;
    private String name;
    private String email;
    private String address;
    private Password password;
    
    public OnlineCustomer(String name, String email, String address, Password password) {
        validateInputs(name, email, address, password);
        this.name = name;
        this.email = email;
        this.address = address;
        this.password = password;
    }
    
    public OnlineCustomer(Long onlineCustomerId, String name, String email, 
                          String address, Password password) {
        this(name, email, address, password);
        this.onlineCustomerId = onlineCustomerId;
    }
    
    private void validateInputs(String name, String email, String address, Password password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
    }
    
    // Getters and setters
    public Long getOnlineCustomerId() {
        return onlineCustomerId;
    }
    
    public void setOnlineCustomerId(Long onlineCustomerId) {
        this.onlineCustomerId = onlineCustomerId;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public void setPassword(Password password) {
        this.password = password;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineCustomer that = (OnlineCustomer) o;
        return Objects.equals(onlineCustomerId, that.onlineCustomerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(onlineCustomerId);
    }
}