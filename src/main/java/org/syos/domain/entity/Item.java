// File: src/main/java/org/syos/domain/entity/Item.java
package org.syos.domain.entity;

import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a product/item in the store.
 * Domain entity with business logic encapsulation.
 */
public class Item {
    private Long itemId;
    private String name;
    private ItemCode code;
    private Money unitPrice;
    private BigDecimal discount; // Percentage (0-100)
    private int reorderLevel;
    
    // Constructor for new items (no ID yet)
    public Item(String name, ItemCode code, Money unitPrice, BigDecimal discount, int reorderLevel) {
        validateInputs(name, code, unitPrice, discount, reorderLevel);
        this.name = name;
        this.code = code;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.reorderLevel = reorderLevel;
    }
    
    // Constructor for existing items (with ID from database)
    public Item(Long itemId, String name, ItemCode code, Money unitPrice, 
                BigDecimal discount, int reorderLevel) {
        this(name, code, unitPrice, discount, reorderLevel);
        this.itemId = itemId;
    }
    
    private void validateInputs(String name, ItemCode code, Money unitPrice, 
                                 BigDecimal discount, int reorderLevel) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (code == null) {
            throw new IllegalArgumentException("Item code cannot be null");
        }
        if (unitPrice == null || unitPrice.isNegative()) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0 || 
            discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }
    }
    
    /**
     * Calculate price after discount for given quantity.
     */
    public Money calculateTotalPrice(int quantity) {
        Money subtotal = unitPrice.multiply(quantity);
        return subtotal.applyDiscount(discount);
    }
    
    /**
     * Check if item needs reordering based on current stock.
     */
    public boolean needsReorder(int currentStock) {
        return currentStock < reorderLevel;
    }
    
    // Getters and setters
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        this.name = name;
    }
    
    public ItemCode getCode() {
        return code;
    }
    
    public void setCode(ItemCode code) {
        if (code == null) {
            throw new IllegalArgumentException("Item code cannot be null");
        }
        this.code = code;
    }
    
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Money unitPrice) {
        if (unitPrice == null || unitPrice.isNegative()) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0 || 
            discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        this.discount = discount;
    }
    
    public int getReorderLevel() {
        return reorderLevel;
    }
    
    public void setReorderLevel(int reorderLevel) {
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }
        this.reorderLevel = reorderLevel;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId) && 
               Objects.equals(code, item.code);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemId, code);
    }
    
    @Override
    public String toString() {
        return String.format("Item[id=%d, name=%s, code=%s, price=%s, discount=%.2f%%]",
            itemId, name, code, unitPrice, discount);
    }
}
