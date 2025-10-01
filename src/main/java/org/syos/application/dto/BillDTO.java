// File: src/main/java/org/syos/application/dto/BillDTO.java
package org.syos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Bill.
 */
public class BillDTO {
    private Long billId;
    private String serialNumber;
    private LocalDateTime dateTime;
    private String transactionType;
    private String customerName;
    private List<BillItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private BigDecimal cashTendered;
    private BigDecimal changeAmount;
    
    // Constructors
    public BillDTO() {}
    
    // Getters and setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public List<BillItemDTO> getItems() { return items; }
    public void setItems(List<BillItemDTO> items) { this.items = items; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BigDecimal getCashTendered() { return cashTendered; }
    public void setCashTendered(BigDecimal cashTendered) { this.cashTendered = cashTendered; }
    
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
}