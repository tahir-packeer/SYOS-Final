// File: src/main/java/org/syos/application/dto/ItemDTO.java
package org.syos.application.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Item.
 * Used for transferring data between layers and for future client-server communication.
 */
public class ItemDTO {
    private Long itemId;
    private String name;
    private String code;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private int reorderLevel;
    
    // Constructors
    public ItemDTO() {}
    
    public ItemDTO(Long itemId, String name, String code, BigDecimal unitPrice, 
                   BigDecimal discount, int reorderLevel) {
        this.itemId = itemId;
        this.name = name;
        this.code = code;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.reorderLevel = reorderLevel;
    }
    
    // Getters and setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
}
