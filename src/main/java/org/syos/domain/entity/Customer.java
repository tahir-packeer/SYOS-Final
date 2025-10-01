// File: src/main/java/org/syos/domain/entity/Customer.java
package org.syos.domain.entity;

import java.util.Objects;

/**
 * Represents a normal (in-store/counter) customer.
 * Separate from online customers as per requirements.
 */
public class Customer {
    private Long customerId;
    private String name;
    private String phone;
    
    public Customer(String name, String phone) {
        validateInputs(name, phone);
        this.name = name;
        this.phone = phone;
    }
    
    public Customer(Long customerId, String name, String phone) {
        this(name, phone);
        this.customerId = customerId;
    }
    
    private void validateInputs(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone cannot be null or empty");
        }
    }
    
    // Getters and setters
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        validateInputs(name, this.phone);
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        validateInputs(this.name, phone);
        this.phone = phone;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
    
    @Override
    public String toString() {
        return String.format("Customer[id=%d, name=%s, phone=%s]", 
            customerId, name, phone);
    }
}