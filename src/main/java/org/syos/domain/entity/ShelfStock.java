// File: src/main/java/org/syos/domain/entity/ShelfStock.java
package org.syos.domain.entity;

import java.util.Objects;

/**
 * Represents items displayed on store shelves (counter sales).
 */
public class ShelfStock {
    private Long shelfStockId;
    private Item item;
    private int quantity;
    
    public ShelfStock(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.item = item;
        this.quantity = quantity;
    }
    
    public ShelfStock(Long shelfStockId, Item item, int quantity) {
        this(item, quantity);
        this.shelfStockId = shelfStockId;
    }
    
    /**
     * Add stock to shelf.
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        this.quantity += quantity;
    }
    
    /**
     * Reduce stock from shelf (after sale).
     */
    public void reduceStock(int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("Cannot reduce more than available quantity");
        }
        this.quantity -= quantity;
    }
    
    /**
     * Check if stock is below reorder level.
     */
    public boolean isBelowReorderLevel() {
        return item.needsReorder(quantity);
    }
    
    // Getters and setters
    public Long getShelfStockId() {
        return shelfStockId;
    }
    
    public void setShelfStockId(Long shelfStockId) {
        this.shelfStockId = shelfStockId;
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
        ShelfStock that = (ShelfStock) o;
        return Objects.equals(shelfStockId, that.shelfStockId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(shelfStockId);
    }
}
