// File: src/main/java/org/syos/controller/ManagerController.java
package org.syos.controller;

import org.syos.application.usecase.*;
import org.syos.domain.entity.*;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for manager operations.
 */
public class ManagerController {
    private final ItemAppService itemService;
    private final StockAppService stockService;
    private final ReportAppService reportService;
    private final CustomerAppService customerService;

    public ManagerController(ItemAppService itemService,
            StockAppService stockService,
            ReportAppService reportService,
            CustomerAppService customerService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.reportService = reportService;
        this.customerService = customerService;
    }

    // Item Management

    public boolean addNewItem(String name, String code, double price,
            double discount, int reorderLevel) {
        try {
            itemService.createItem(name, code, BigDecimal.valueOf(price),
                    BigDecimal.valueOf(discount), reorderLevel);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding item: " + e.getMessage());
            return false;
        }
    }

    public void displayAllItems() {
        try {
            List<Item> items = itemService.getAllItems();

            System.out.println("\n" + "=".repeat(90));
            System.out.printf("%-10s %-30s %-15s %-12s %-10s\n",
                    "Code", "Name", "Price", "Discount", "Reorder");
            System.out.println("=".repeat(90));

            for (Item item : items) {
                System.out.printf("%-10s %-30s %-15s %-12.2f%% %-10d\n",
                        item.getCode().getCode(),
                        truncate(item.getName(), 30),
                        item.getUnitPrice().toDisplayString(),
                        item.getDiscount().doubleValue(),
                        item.getReorderLevel());
            }

            System.out.println("=".repeat(90));
            System.out.println("Total items: " + items.size());

        } catch (Exception e) {
            System.err.println("Error displaying items: " + e.getMessage());
        }
    }

    public void searchItemByCode(String code) {
        try {
            var itemOpt = itemService.findItemByCode(code);

            if (itemOpt.isEmpty()) {
                System.out.println("\nItem not found: " + code);
                return;
            }

            Item item = itemOpt.get();
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Item Details");
            System.out.println("=".repeat(50));
            System.out.println("ID: " + item.getItemId());
            System.out.println("Name: " + item.getName());
            System.out.println("Code: " + item.getCode());
            System.out.println("Unit Price: " + item.getUnitPrice().toDisplayString());
            System.out.println("Discount: " + item.getDiscount() + "%");
            System.out.println("Reorder Level: " + item.getReorderLevel());
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("Error searching item: " + e.getMessage());
        }
    }

    // Stock Management

    public boolean receiveStock(String itemCode, int quantity,
            String purchaseDate, String expiryDate) {
        try {
            LocalDate purchase = LocalDate.parse(purchaseDate);
            LocalDate expiry = expiryDate != null ? LocalDate.parse(expiryDate) : null;

            stockService.receiveStock(itemCode, quantity, purchase, expiry);
            return true;
        } catch (Exception e) {
            System.err.println("Error receiving stock: " + e.getMessage());
            return false;
        }
    }

    public boolean moveToShelf(String itemCode, int quantity) {
        try {
            stockService.moveToShelf(itemCode, quantity);
            return true;
        } catch (Exception e) {
            System.err.println("Error moving stock: " + e.getMessage());
            return false;
        }
    }

    public boolean moveToWebsite(String itemCode, int quantity) {
        try {
            stockService.moveToWebsite(itemCode, quantity);
            return true;
        } catch (Exception e) {
            System.err.println("Error moving stock to website: " + e.getMessage());
            return false;
        }
    }

    public void displayStockBatches() {
        try {
            List<StockBatch> batches = stockService.getAllStockBatches();

            System.out.println("\n" + "=".repeat(100));
            System.out.printf("%-10s %-25s %-12s %-12s %-15s %-15s\n",
                    "Code", "Item", "Received", "Remaining", "Purchase Date", "Expiry Date");
            System.out.println("=".repeat(100));

            for (StockBatch batch : batches) {
                System.out.printf("%-10s %-25s %-12d %-12d %-15s %-15s\n",
                        batch.getItem().getCode().getCode(),
                        truncate(batch.getItem().getName(), 25),
                        batch.getQuantityReceived(),
                        batch.getQuantityRemaining(),
                        batch.getPurchaseDate(),
                        batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : "N/A");
            }

            System.out.println("=".repeat(100));
            System.out.println("Total batches: " + batches.size());

        } catch (Exception e) {
            System.err.println("Error displaying batches: " + e.getMessage());
        }
    }

    public void displayShelfStock() {
        try {
            List<org.syos.domain.entity.ShelfStock> shelfStocks = stockService.getAllShelfStock();

            if (shelfStocks.isEmpty()) {
                System.out.println("No items on shelf.");
                return;
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.printf("%-15s %-30s %-15s %-15s%n",
                    "ITEM CODE", "ITEM NAME", "SHELF QTY", "REORDER LEVEL");
            System.out.println("-".repeat(80));

            for (org.syos.domain.entity.ShelfStock stock : shelfStocks) {
                org.syos.domain.entity.Item item = stock.getItem();
                String status = stock.getQuantity() < item.getReorderLevel() ? " (LOW STOCK!)" : "";

                System.out.printf("%-15s %-30s %-15d %-15d%s%n",
                        item.getCode().getCode(),
                        truncate(item.getName(), 30),
                        stock.getQuantity(),
                        item.getReorderLevel(),
                        status);
            }
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("Error displaying shelf stock: " + e.getMessage());
        }
    }

    public void displayWebsiteInventory() {
        try {
            List<org.syos.domain.entity.WebsiteInventory> websiteInventories = stockService.getAllWebsiteInventory();

            if (websiteInventories.isEmpty()) {
                System.out.println("No items in website inventory.");
                return;
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.printf("%-15s %-30s %-15s %-15s%n",
                    "ITEM CODE", "ITEM NAME", "WEB QTY", "REORDER LEVEL");
            System.out.println("-".repeat(80));

            for (org.syos.domain.entity.WebsiteInventory inventory : websiteInventories) {
                org.syos.domain.entity.Item item = inventory.getItem();
                String status = inventory.getQuantity() < item.getReorderLevel() ? " (LOW STOCK!)" : "";

                System.out.printf("%-15s %-30s %-15d %-15d%s%n",
                        item.getCode().getCode(),
                        truncate(item.getName(), 30),
                        inventory.getQuantity(),
                        item.getReorderLevel(),
                        status);
            }
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("Error displaying website inventory: " + e.getMessage());
        }
    }

    // Reports

    public void displayDailySalesReport(String date) {
        try {
            LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            ReportAppService.DailySalesReport report = reportService.generateDailySalesReport(reportDate, null);

            printDailySalesReport(report);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    public void displayCounterSalesReport(String date) {
        try {
            LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            ReportAppService.DailySalesReport report = reportService.generateDailySalesReport(reportDate,
                    TransactionType.COUNTER);

            printDailySalesReport(report);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    public void displayOnlineSalesReport(String date) {
        try {
            LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            ReportAppService.DailySalesReport report = reportService.generateDailySalesReport(reportDate,
                    TransactionType.ONLINE);

            printDailySalesReport(report);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private void printDailySalesReport(ReportAppService.DailySalesReport report) {
        System.out.println("\nDate: " + report.getDate());
        System.out.println("Transaction Type: " +
                (report.getTransactionType() != null ? report.getTransactionType().getDisplayName() : "All"));

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("%-10s %-35s %-15s %-15s\n",
                "Code", "Item Name", "Quantity", "Revenue");
        System.out.println("=".repeat(80));

        for (ReportAppService.SalesItemSummary summary : report.getItemSummaries()) {
            System.out.printf("%-10s %-35s %-15d %-15s\n",
                    summary.getItemCode(),
                    truncate(summary.getItemName(), 35),
                    summary.getTotalQuantity(),
                    summary.getTotalRevenue().toDisplayString());
        }

        System.out.println("=".repeat(80));
        System.out.println("Total Revenue: " + report.getTotalRevenue().toDisplayString());
    }

    public void displayStockReport() {
        displayStockBatches();
    }

    public void displayReorderReport() {
        try {
            List<ShelfStock> items = stockService.getItemsBelowReorderLevel();

            System.out.println("\n" + "=".repeat(70));
            System.out.printf("%-10s %-30s %-15s %-10s\n",
                    "Code", "Item Name", "Current Stock", "Reorder Level");
            System.out.println("=".repeat(70));

            for (ShelfStock stock : items) {
                System.out.printf("%-10s %-30s %-15d %-10d\n",
                        stock.getItem().getCode().getCode(),
                        truncate(stock.getItem().getName(), 30),
                        stock.getQuantity(),
                        stock.getItem().getReorderLevel());
            }

            System.out.println("=".repeat(70));
            System.out.println("Items below reorder level: " + items.size());

        } catch (Exception e) {
            System.err.println("Error generating reorder report: " + e.getMessage());
        }
    }

    public void displayPendingShelvingReport() {
        try {
            ReportAppService.PendingShelvingReport report = reportService.generatePendingShelvingReport();
            List<StockBatch> pendingBatches = report.getPendingBatches();

            System.out.println("\n" + "=".repeat(100));
            System.out.println("                              PENDING SHELVING REPORT");
            System.out.println("=".repeat(100));
            System.out.printf("%-12s %-25s %-10s %-15s %-15s %-15s\n",
                    "Code", "Item Name", "Batch ID", "Available Qty", "Purchase Date", "Expiry Date");
            System.out.println("=".repeat(100));

            for (StockBatch batch : pendingBatches) {
                System.out.printf("%-12s %-25s %-10d %-15d %-15s %-15s\n",
                        batch.getItem().getCode().getCode(),
                        truncate(batch.getItem().getName(), 25),
                        batch.getBatchId(),
                        batch.getQuantityRemaining(),
                        batch.getPurchaseDate(),
                        batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : "No expiry");
            }

            System.out.println("=".repeat(100));
            System.out.printf("Total pending batches: %d | Total quantity available for shelving: %d\n",
                    report.getTotalPendingBatches(), report.getTotalPendingQuantity());
            System.out.println("=".repeat(100));

        } catch (Exception e) {
            System.err.println("Error generating pending shelving report: " + e.getMessage());
        }
    }

    public void displayBillReport() {
        try {
            // Get date range for the report (last 30 days by default)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            ReportAppService.BillReport report = reportService.generateBillReport(startDate, endDate);

            System.out.println("\nDate Range: " + startDate + " to " + endDate);
            System.out.println("Total Bills: " + report.getBills().size());

            if (report.getBills().isEmpty()) {
                System.out.println("No bills found in this period.");
                return;
            }

            // Display bill summary
            System.out.println("\n" + "=".repeat(120));
            System.out.printf("%-20s %-15s %-15s %-20s %-25s %-15s%n",
                    "Bill Number", "Date", "Type", "Customer", "Total Amount", "Payment");
            System.out.println("=".repeat(120));

            Money grandTotal = Money.zero();

            for (Bill bill : report.getBills()) {
                System.out.printf("%-20s %-15s %-15s %-20s %-25s %-15s%n",
                        bill.getSerialNumber(),
                        bill.getDateTime().toLocalDate(),
                        bill.getTransactionType(),
                        bill.getCustomerName() != null ? bill.getCustomerName() : "Walk-in",
                        "Rs " + bill.getTotalAmount(),
                        bill.getPaymentMethod());

                grandTotal = grandTotal.add(bill.getTotalAmount());
            }

            System.out.println("=".repeat(120));
            System.out.println("GRAND TOTAL: Rs " + grandTotal);

        } catch (Exception e) {
            System.err.println("Error generating bill report: " + e.getMessage());
        }
    }

    // Customer Management

    public boolean registerCustomer(String name, String phone) {
        try {
            customerService.registerCustomer(name, phone);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering customer: " + e.getMessage());
            return false;
        }
    }

    public void displayAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();

            System.out.println("\n" + "=".repeat(60));
            System.out.printf("%-10s %-30s %-15s\n", "ID", "Name", "Phone");
            System.out.println("=".repeat(60));

            for (Customer customer : customers) {
                System.out.printf("%-10d %-30s %-15s\n",
                        customer.getCustomerId(),
                        truncate(customer.getName(), 30),
                        customer.getPhone());
            }

            System.out.println("=".repeat(60));
            System.out.println("Total customers: " + customers.size());

        } catch (Exception e) {
            System.err.println("Error displaying customers: " + e.getMessage());
        }
    }

    public void searchCustomerByPhone(String phone) {
        try {
            var customerOpt = customerService.findByPhone(phone);

            if (customerOpt.isEmpty()) {
                System.out.println("\nCustomer not found with phone: " + phone);
                return;
            }

            Customer customer = customerOpt.get();
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Customer Details");
            System.out.println("=".repeat(50));
            System.out.println("ID: " + customer.getCustomerId());
            System.out.println("Name: " + customer.getName());
            System.out.println("Phone: " + customer.getPhone());
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("Error searching customer: " + e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
