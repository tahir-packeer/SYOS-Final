// File: src/main/java/org/syos/controller/CashierController.java
package org.syos.controller;

import org.syos.application.usecase.*;
import org.syos.domain.entity.*;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import org.syos.infrastructure.util.LoggingService;
import java.time.LocalDate;
import java.util.List;

/**
 * Enhanced controller for cashier operations.
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
        LoggingService.logControllerEntry("CashierController", "processCounterSale",
                customerName, String.valueOf(cashAmount));
        boolean result = processCounterSale(orderItems, customerName, cashAmount, 0.0);
        LoggingService.logControllerExit("CashierController", "processCounterSale");
        return result;
    }

    /**
     * Process a counter sale with manual discount.
     */
    public boolean processCounterSale(List<BillingAppService.OrderItem> orderItems,
            String customerName, double cashAmount, double discountAmount) {
        LoggingService.logControllerEntry("CashierController", "processCounterSale",
                customerName, String.valueOf(cashAmount), String.valueOf(discountAmount));
        LoggingService.setOperation("COUNTER_SALE");
        LoggingService.PerformanceMonitor saleMonitor = LoggingService
                .startPerformanceMonitoring("Process Counter Sale");

        try {
            Money cashTendered = Money.of(cashAmount);
            Money manualDiscount = Money.of(discountAmount);

            // Log the sale attempt
            LoggingService.setOperation("PROCESSING_SALE");

            billingService.processCounterSale(orderItems, customerName, cashTendered, manualDiscount);

            // Log successful sale
            LoggingService.logPaymentProcessed("COUNTER_SALE", "CASH", cashAmount);
            saleMonitor.complete();
            LoggingService.logControllerExit("CashierController", "processCounterSale");

            return true;
        } catch (Exception e) {
            LoggingService.logError("Error processing counter sale for customer: " + customerName, e);
            saleMonitor.completeWithError(e);
            System.err.println("Error processing sale: " + e.getMessage());
            LoggingService.logControllerExit("CashierController", "processCounterSale");
            return false;
        }
    }

    /**
     * Preview bill totals before payment.
     */
    public BillingAppService.BillPreview previewBill(List<BillingAppService.OrderItem> orderItems,
            String customerName, double discountAmount) {
        LoggingService.logControllerEntry("CashierController", "previewBill", customerName,
                String.valueOf(discountAmount));
        LoggingService.setOperation("BILL_PREVIEW");

        try {
            Money manualDiscount = Money.of(discountAmount);
            BillingAppService.BillPreview preview = billingService.previewBill(orderItems, customerName,
                    manualDiscount);
            LoggingService.logControllerExit("CashierController", "previewBill");
            return preview;
        } catch (Exception e) {
            LoggingService.logError("Error generating bill preview for customer: " + customerName, e);
            System.err.println("Error generating preview: " + e.getMessage());
            LoggingService.logControllerExit("CashierController", "previewBill");
            return null;
        }
    }

    /**
     * Find or register customer by phone number.
     */
    public java.util.Optional<org.syos.domain.entity.Customer> findOrRegisterCustomer(String customerName,
            String phoneNumber) {
        LoggingService.logControllerEntry("CashierController", "findOrRegisterCustomer", customerName, phoneNumber);
        LoggingService.setOperation("CUSTOMER_LOOKUP");

        try {
            java.util.Optional<org.syos.domain.entity.Customer> customer = billingService
                    .findOrRegisterCustomer(customerName, phoneNumber);

            if (customer.isPresent()) {
                LoggingService.logDatabaseOperation("FOUND", "Customer", customer.get().getCustomerId().toString());
            } else {
                LoggingService.logCustomerRegistered(phoneNumber, "WALK_IN");
            }

            LoggingService.logControllerExit("CashierController", "findOrRegisterCustomer");
            return customer;
        } catch (Exception e) {
            LoggingService.logError("Error finding/registering customer: " + customerName, e);
            LoggingService.logControllerExit("CashierController", "findOrRegisterCustomer");
            throw e;
        }
    }

    /**
     * Display all items.
     */
    public void displayAllItems() {
        LoggingService.logControllerEntry("CashierController", "displayAllItems");
        LoggingService.setOperation("DISPLAY_ITEMS");
        LoggingService.PerformanceMonitor displayMonitor = LoggingService
                .startPerformanceMonitoring("Display All Items");

        try {
            List<Item> items = itemService.getAllItems();
            LoggingService.logDatabaseOperation("SELECT", "Item", "all");

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
                        item.getReorderLevel());
            }

            System.out.println("=".repeat(80));
            System.out.println("Total items: " + items.size());

            displayMonitor.complete();
            LoggingService.logControllerExit("CashierController", "displayAllItems");

        } catch (Exception e) {
            LoggingService.logError("Error displaying items", e);
            displayMonitor.completeWithError(e);
            System.err.println("Error displaying items: " + e.getMessage());
            LoggingService.logControllerExit("CashierController", "displayAllItems");
        }
    }

    /**
     * Check stock for an item.
     */
    public void checkItemStock(String itemCode) {
        LoggingService.logControllerEntry("CashierController", "checkItemStock", itemCode);
        LoggingService.setOperation("STOCK_CHECK");

        try {
            var itemOpt = itemService.findItemByCode(itemCode);
            LoggingService.logDatabaseOperation("SELECT", "Item", itemCode);

            if (itemOpt.isEmpty()) {
                LoggingService.logValidationError("CashierController", "itemCode", itemCode, "Item not found");
                System.out.println("\nItem not found: " + itemCode);
                LoggingService.logControllerExit("CashierController", "checkItemStock");
                return;
            }

            Item item = itemOpt.get();
            System.out.println("\nItem: " + item.getName());
            System.out.println("Code: " + item.getCode());
            System.out.println("Price: " + item.getUnitPrice().toDisplayString());
            System.out.println("Discount: " + item.getDiscount() + "%");
            System.out.println("Reorder Level: " + item.getReorderLevel());

            LoggingService.logControllerExit("CashierController", "checkItemStock");

        } catch (Exception e) {
            LoggingService.logError("Error checking stock for item: " + itemCode, e);
            System.err.println("Error checking stock: " + e.getMessage());
            LoggingService.logControllerExit("CashierController", "checkItemStock");
        }
    }

    /**
     * Display today's sales report.
     */
    public void displayTodaysSalesReport() {
        LoggingService.logControllerEntry("CashierController", "displayTodaysSalesReport");
        LoggingService.setOperation("SALES_REPORT");
        LoggingService.PerformanceMonitor reportMonitor = LoggingService
                .startPerformanceMonitoring("Generate Sales Report");

        try {
            LocalDate today = LocalDate.now();
            ReportAppService.DailySalesReport report = reportService.generateDailySalesReport(today,
                    TransactionType.COUNTER);

            LoggingService.logDatabaseOperation("SELECT", "SalesReport", today.toString());

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
                        summary.getTotalRevenue().toDisplayString());
            }

            System.out.println("=".repeat(80));
            System.out.println("Total Revenue: " + report.getTotalRevenue().toDisplayString());

            reportMonitor.complete();
            LoggingService.logControllerExit("CashierController", "displayTodaysSalesReport");

        } catch (Exception e) {
            LoggingService.logError("Error generating daily sales report", e);
            reportMonitor.completeWithError(e);
            System.err.println("Error generating report: " + e.getMessage());
            LoggingService.logControllerExit("CashierController", "displayTodaysSalesReport");
        }
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
