// File: src/main/java/org/syos/domain/entity/BillItem.java
package org.syos.domain.entity;

import org.syos.domain.valueobject.Money;
import java.util.Objects;

/**
 * Represents a line item in a bill.
 */
public class BillItem {
    private Long billItemId;
    private Item item;
    private int quantity;
    private Money unitPrice;
    private Money totalPrice;
    
    public BillItem(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = item.getUnitPrice();
        this.totalPrice = item.calculateTotalPrice(quantity);
    }
    
    public BillItem(Long billItemId, Item item, int quantity, Money unitPrice, Money totalPrice) {
        this.billItemId = billItemId;
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
    
    // Getters
    public Long getBillItemId() {
        return billItemId;
    }
    
    public void setBillItemId(Long billItemId) {
        this.billItemId = billItemId;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    public Money getTotalPrice() {
        return totalPrice;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItem billItem = (BillItem) o;
        return Objects.equals(billItemId, billItem.billItemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(billItemId);
    }
}