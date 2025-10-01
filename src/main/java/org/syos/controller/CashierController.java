// File: src/main/java/org/syos/controller/CashierController.java
package org.syos.controller;

import org.syos.application.usecase.*;
import org.syos.domain.entity.*;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for cashier operations.
 * Delegates to application services.
 */
public class CashierController {
    private final BillingAppService billingService;
    private final ItemAppService itemService;
    private final ReportAppService reportService;
    
    public CashierController(BillingAppService billingService,
                            ItemAppService itemService,
                            ReportAppService reportService) {
        this.billingService = billingService;
        this.itemService = itemService;
        this.reportService = reportService;
    }
    
    /**
     * Process a counter sale.
     */
    public boolean processCounterSale(List<BillingAppService.OrderItem> orderItems,
                                     String customerName, double cashAmount) {
        try {
            Money cashTendered = Money.of(cashAmount);
            billingService.processCounterSale(orderItems, customerName, cashTendered);
            return true;
        } catch (Exception e) {
            System.err.println("Error processing sale: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Display all items.
     */
    public void displayAllItems() {
        try {
            List<Item> items = itemService.getAllItems();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.printf("%-10s %-25s %-15s %-15s %-10s\n",
                "Code", "Name", "Price", "Discount", "Reorder");
            System.out.println("=".repeat(80));
            
            for (Item item : items) {
                System.out.printf("%-10s %-25s %-15s %-15.2f%% %-10d\n",
                    item.getCode().getCode(),
                    truncate(item.getName(), 25),
                    item.getUnitPrice().toDisplayString(),
                    item.getDiscount().doubleValue(),
                    item.getReorderLevel()
                );
            }
            
            System.out.println("=".repeat(80));
            System.out.println("Total items: " + items.size());
            
        } catch (Exception e) {
            System.err.println("Error displaying items: " + e.getMessage());
        }
    }
    
    /**
     * Check stock for an item.
     */
    public void checkItemStock(String itemCode) {
        try {
            var itemOpt = itemService.findItemByCode(itemCode);
            
            if (itemOpt.isEmpty()) {
                System.out.println("\nItem not found: " + itemCode);
                return;
            }
            
            Item item = itemOpt.get();
            System.out.println("\nItem: " + item.getName());
            System.out.println("Code: " + item.getCode());
            System.out.println("Price: " + item.getUnitPrice().toDisplayString());
            System.out.println("Discount: " + item.getDiscount() + "%");
            System.out.println("Reorder Level: " + item.getReorderLevel());
            
        } catch (Exception e) {
            System.err.println("Error checking stock: " + e.getMessage());
        }
    }
    
    /**
     * Display today's sales report.
     */
    public void displayTodaysSalesReport() {
        try {
            LocalDate today = LocalDate.now();
            ReportAppService.DailySalesReport report = 
                reportService.generateDailySalesReport(today, TransactionType.COUNTER);
            
            System.out.println("\nDate: " + report.getDate());
            System.out.println("Transaction Type: Counter Sales");
            System.out.println("\n" + "=".repeat(80));
            System.out.printf("%-10s %-30s %-15s %-15s\n",
                "Code", "Item Name", "Quantity", "Revenue");
            System.out.println("=".repeat(80));
            
            for (ReportAppService.SalesItemSummary summary : report.getItemSummaries()) {
                System.out.printf("%-10s %-30s %-15d %-15s\n",
                    summary.getItemCode(),
                    truncate(summary.getItemName(), 30),
                    summary.getTotalQuantity(),
                    summary.getTotalRevenue().toDisplayString()
                );
            }
            
            System.out.println("=".repeat(80));
            System.out.println("Total Revenue: " + report.getTotalRevenue().toDisplayString());
            
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }
    
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}