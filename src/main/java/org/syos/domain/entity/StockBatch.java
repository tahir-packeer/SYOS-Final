// File: src/main/java/org/syos/domain/entity/StockBatch.java
package org.syos.domain.entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a batch of stock received from suppliers.
 */
public class StockBatch {
    private Long batchId;
    private Item item;
    private int quantityReceived;
    private int quantityRemaining;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    
    public StockBatch(Item item, int quantityReceived, LocalDate purchaseDate, LocalDate expiryDate) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantityReceived <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null");
        }
        if (expiryDate != null && expiryDate.isBefore(purchaseDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before purchase date");
        }
        
        this.item = item;
        this.quantityReceived = quantityReceived;
        this.quantityRemaining = quantityReceived;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }
    
    public StockBatch(Long batchId, Item item, int quantityReceived, int quantityRemaining,
                      LocalDate purchaseDate, LocalDate expiryDate) {
        this(item, quantityReceived, purchaseDate, expiryDate);
        this.batchId = batchId;
        this.quantityRemaining = quantityRemaining;
    }
    
    /**
     * Reduce stock from this batch.
     */
    public void reduceStock(int quantity) {
        if (quantity > quantityRemaining) {
            throw new IllegalArgumentException("Cannot reduce more than remaining quantity");
        }
        this.quantityRemaining -= quantity;
    }
    
    /**
     * Check if batch is expired.
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
    
    /**
     * Calculate days until expiry.
     */
    public long daysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    // Getters and setters
    public Long getBatchId() {
        return batchId;
    }
    
    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getQuantityReceived() {
        return quantityReceived;
    }
    
    public int getQuantityRemaining() {
        return quantityRemaining;
    }
    
    public void setQuantityRemaining(int quantityRemaining) {
        this.quantityRemaining = quantityRemaining;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockBatch that = (StockBatch) o;
        return Objects.equals(batchId, that.batchId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(batchId);
    }
}