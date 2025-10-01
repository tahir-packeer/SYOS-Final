// File: src/main/java/org/syos/domain/entity/WebsiteInventory.java
package org.syos.domain.entity;

import java.util.Objects;

/**
 * Represents items available for online sales (website inventory).
 * Separate from shelf stock as per requirements.
 */
public class WebsiteInventory {
    private Long webInventoryId;
    private Item item;
    private int quantity;
    
    public WebsiteInventory(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.item = item;
        this.quantity = quantity;
    }
    
    public WebsiteInventory(Long webInventoryId, Item item, int quantity) {
        this(item, quantity);
        this.webInventoryId = webInventoryId;
    }
    
    /**
     * Add stock to website inventory.
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        this.quantity += quantity;
    }
    
    /**
     * Reduce stock from website inventory (after online sale).
     */
    public void reduceStock(int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("Cannot reduce more than available quantity");
        }
        this.quantity -= quantity;
    }
    
    /**
     * Check if item is in stock for online orders.
     */
    public boolean isInStock(int requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }
    
    // Getters and setters
    public Long getWebInventoryId() {
        return webInventoryId;
    }
    
    public void setWebInventoryId(Long webInventoryId) {
        this.webInventoryId = webInventoryId;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebsiteInventory that = (WebsiteInventory) o;
        return Objects.equals(webInventoryId, that.webInventoryId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(webInventoryId);
    }
}