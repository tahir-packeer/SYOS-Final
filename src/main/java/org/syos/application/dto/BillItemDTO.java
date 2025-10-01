// File: src/main/java/org/syos/application/dto/BillItemDTO.java
package org.syos.application.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for BillItem.
 */
public class BillItemDTO {
    private String itemName;
    private String itemCode;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    public BillItemDTO() {}
    
    public BillItemDTO(String itemName, String itemCode, int quantity, 
                       BigDecimal unitPrice, BigDecimal totalPrice) {
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
    
    // Getters and setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
