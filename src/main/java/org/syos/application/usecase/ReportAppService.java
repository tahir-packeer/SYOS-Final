// File: src/main/java/org/syos/application/usecase/ReportAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.*;
import org.syos.application.service.Logger;
import org.syos.domain.entity.*;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service for generating reports.
 */
public class ReportAppService {
    private final BillRepository billRepository;
    private final StockBatchRepository stockBatchRepository;
    private final ShelfStockRepository shelfStockRepository;
    private final Logger logger;
    
    public ReportAppService(BillRepository billRepository,
                           StockBatchRepository stockBatchRepository,
                           ShelfStockRepository shelfStockRepository,
                           Logger logger) {
        this.billRepository = billRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.shelfStockRepository = shelfStockRepository;
        this.logger = logger;
    }
    
    /**
     * Generate daily sales report.
     */
    public DailySalesReport generateDailySalesReport(LocalDate date, 
                                                     TransactionType transactionType) {
        try {
            List<Bill> bills;
            
            if (transactionType == null) {
                bills = billRepository.findByDate(date);
            } else {
                bills = billRepository.findByDateAndType(date, transactionType);
            }
            
            Map<String, SalesItemSummary> itemSummaries = new HashMap<>();
            Money totalRevenue = Money.zero();
            
            for (Bill bill : bills) {
                totalRevenue = totalRevenue.add(bill.getTotalAmount());
                
                for (BillItem billItem : bill.getItems()) {
                    String itemCode = billItem.getItem().getCode().toString();
                    String itemName = billItem.getItem().getName();
                    
                    SalesItemSummary summary = itemSummaries.getOrDefault(
                        itemCode, 
                        new SalesItemSummary(itemCode, itemName)
                    );
                    
                    summary.addSale(billItem.getQuantity(), billItem.getTotalPrice());
                    itemSummaries.put(itemCode, summary);
                }
            }
            
            return new DailySalesReport(date, transactionType, totalRevenue, 
                                       new ArrayList<>(itemSummaries.values()));
                                       
        } catch (Exception e) {
            logger.error("Error generating daily sales report", e);
            throw new RuntimeException("Failed to generate sales report", e);
        }
    }
    
    /**
     * Generate stock report.
     */
    public StockReport generateStockReport() {
        try {
            List<StockBatch> batches = stockBatchRepository.findAll();
            return new StockReport(batches);
        } catch (Exception e) {
            logger.error("Error generating stock report", e);
            throw new RuntimeException("Failed to generate stock report", e);
        }
    }
    
    /**
     * Generate reorder level report.
     */
    public ReorderReport generateReorderReport() {
        try {
            List<ShelfStock> itemsBelowReorder = shelfStockRepository.findBelowReorderLevel();
            return new ReorderReport(itemsBelowReorder);
        } catch (Exception e) {
            logger.error("Error generating reorder report", e);
            throw new RuntimeException("Failed to generate reorder report", e);
        }
    }
    
    /**
     * Generate bill report (all transactions).
     */
    public BillReport generateBillReport(LocalDate startDate, LocalDate endDate) {
        try {
            List<Bill> allBills = billRepository.findAll();
            
            // Filter by date range if provided
            if (startDate != null && endDate != null) {
                allBills = allBills.stream()
                    .filter(bill -> {
                        LocalDate billDate = bill.getDateTime().toLocalDate();
                        return !billDate.isBefore(startDate) && !billDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
            }
            
            return new BillReport(allBills);
        } catch (Exception e) {
            logger.error("Error generating bill report", e);
            throw new RuntimeException("Failed to generate bill report", e);
        }
    }
    
    // Report DTOs
    
    public static class DailySalesReport {
        private final LocalDate date;
        private final TransactionType transactionType;
        private final Money totalRevenue;
        private final List<SalesItemSummary> itemSummaries;
        
        public DailySalesReport(LocalDate date, TransactionType transactionType,
                               Money totalRevenue, List<SalesItemSummary> itemSummaries) {
            this.date = date;
            this.transactionType = transactionType;
            this.totalRevenue = totalRevenue;
            this.itemSummaries = itemSummaries;
        }
        
        public LocalDate getDate() { return date; }
        public TransactionType getTransactionType() { return transactionType; }
        public Money getTotalRevenue() { return totalRevenue; }
        public List<SalesItemSummary> getItemSummaries() { return itemSummaries; }
    }
    
    public static class SalesItemSummary {
        private final String itemCode;
        private final String itemName;
        private int totalQuantity;
        private Money totalRevenue;
        
        public SalesItemSummary(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.totalQuantity = 0;
            this.totalRevenue = Money.zero();
        }
        
        public void addSale(int quantity, Money revenue) {
            this.totalQuantity += quantity;
            this.totalRevenue = this.totalRevenue.add(revenue);
        }
        
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getTotalQuantity() { return totalQuantity; }
        public Money getTotalRevenue() { return totalRevenue; }
    }
    
    public static class StockReport {
        private final List<StockBatch> batches;
        
        public StockReport(List<StockBatch> batches) {
            this.batches = batches;
        }
        
        public List<StockBatch> getBatches() { return batches; }
    }
    
    public static class ReorderReport {
        private final List<ShelfStock> itemsBelowReorder;
        
        public ReorderReport(List<ShelfStock> itemsBelowReorder) {
            this.itemsBelowReorder = itemsBelowReorder;
        }
        
        public List<ShelfStock> getItemsBelowReorder() { return itemsBelowReorder; }
    }
    
    public static class BillReport {
        private final List<Bill> bills;
        
        public BillReport(List<Bill> bills) {
            this.bills = bills;
        }
        
        public List<Bill> getBills() { return bills; }
    }
}
