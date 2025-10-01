// File: src/main/java/org/syos/infrastructure/external/ConsoleBillPrinter.java
package org.syos.infrastructure.external;

import org.syos.application.service.BillPrinter;
import org.syos.domain.entity.Bill;
import org.syos.domain.entity.BillItem;
import java.time.format.DateTimeFormatter;

/**
 * Console implementation of BillPrinter.
 */
public class ConsoleBillPrinter implements BillPrinter {
    
    @Override
    public void print(Bill bill) {
        System.out.println(formatBill(bill));
    }
    
    @Override
    public String formatBill(Bill bill) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("        SYNEX OUTLET STORE           \n");
        sb.append("         Colombo, Sri Lanka          \n");
        sb.append("=====================================\n");
        sb.append("\n");
        sb.append("Bill Serial No: ").append(bill.getSerialNumber()).append("\n");
        sb.append("Date: ").append(bill.getDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Transaction Type: ").append(bill.getTransactionType().getDisplayName())
            .append("\n");
        
        if (bill.getCustomerName() != null) {
            sb.append("Customer: ").append(bill.getCustomerName()).append("\n");
        }
        
        sb.append("\n");
        sb.append("-------------------------------------\n");
        sb.append(String.format("%-20s %5s %10s %12s\n", 
            "Item", "Qty", "Price", "Total"));
        sb.append("-------------------------------------\n");
        
        for (BillItem item : bill.getItems()) {
            sb.append(String.format("%-20s %5d %10s %12s\n",
                truncate(item.getItem().getName(), 20),
                item.getQuantity(),
                item.getUnitPrice().toDisplayString(),
                item.getTotalPrice().toDisplayString()
            ));
        }
        
        sb.append("-------------------------------------\n");
        sb.append(String.format("%-36s %12s\n", "Subtotal:", 
            bill.getSubtotal().toDisplayString()));
        
        if (!bill.getDiscount().isZero()) {
            sb.append(String.format("%-36s %12s\n", "Discount:", 
                bill.getDiscount().toDisplayString()));
        }
        
        sb.append(String.format("%-36s %12s\n", "TOTAL:", 
            bill.getTotalAmount().toDisplayString()));
        sb.append("\n");
        
        sb.append("Payment Method: ").append(bill.getPaymentMethod().getDisplayName())
            .append("\n");
        
        if (bill.getCashTendered() != null) {
            sb.append(String.format("%-36s %12s\n", "Cash Tendered:", 
                bill.getCashTendered().toDisplayString()));
            sb.append(String.format("%-36s %12s\n", "Change:", 
                bill.getChangeAmount().toDisplayString()));
        }
        
        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("    Thank you for shopping with us!  \n");
        sb.append("=====================================\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}