// File: src/main/java/org/syos/application/usecase/StockAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.*;
import org.syos.application.service.Logger;
import org.syos.domain.entity.*;
import org.syos.domain.valueobject.ItemCode;
import org.syos.infrastructure.persistence.TransactionManager;
import java.time.LocalDate;
import java.util.*;

/**
 * Application service for stock management.
 * Implements shelving strategy as per requirements.
 */
public class StockAppService {
    private final ItemRepository itemRepository;
    private final StockBatchRepository stockBatchRepository;
    private final ShelfStockRepository shelfStockRepository;
    private final WebsiteInventoryRepository websiteInventoryRepository;
    private final TransactionManager transactionManager;
    private final Logger logger;
    
    public StockAppService(ItemRepository itemRepository,
                          StockBatchRepository stockBatchRepository,
                          ShelfStockRepository shelfStockRepository,
                          WebsiteInventoryRepository websiteInventoryRepository,
                          TransactionManager transactionManager,
                          Logger logger) {
        this.itemRepository = itemRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.shelfStockRepository = shelfStockRepository;
        this.websiteInventoryRepository = websiteInventoryRepository;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }
    
    /**
     * Receive new stock batch.
     */
    public StockBatch receiveStock(String itemCode, int quantity, 
                                  LocalDate purchaseDate, LocalDate expiryDate) {
        return transactionManager.executeInTransaction(conn -> {
            try {
                Optional<Item> itemOpt = itemRepository.findByCode(new ItemCode(itemCode));
                
                if (itemOpt.isEmpty()) {
                    throw new IllegalArgumentException("Item not found: " + itemCode);
                }
                
                Item item = itemOpt.get();
                StockBatch batch = new StockBatch(item, quantity, purchaseDate, expiryDate);
                
                StockBatch savedBatch = stockBatchRepository.save(batch);
                logger.info("Received stock batch: " + quantity + " units of " + item.getName());
                
                return savedBatch;
                
            } catch (Exception e) {
                logger.error("Error receiving stock", e);
                throw new RuntimeException("Failed to receive stock", e);
            }
        });
    }
    
    /**
     * Move stock from batches to shelf.
     * Implements FEFO (First Expired First Out) strategy with fallback to FIFO.
     */
    public void moveToShelf(String itemCode, int quantityToMove) {
        transactionManager.executeInTransaction(conn -> {
            try {
                ItemCode code = new ItemCode(itemCode);
                
                // Get all batches for this item, ordered
                List<StockBatch> batches = stockBatchRepository.findByItemCodeOrderedByDate(code);
                
                if (batches.isEmpty()) {
                    throw new IllegalArgumentException("No stock batches available for: " + itemCode);
                }
                
                // Apply shelving strategy
                List<StockBatch> batchesToUse = selectBatchesForShelving(batches, quantityToMove);
                
                int remainingQty = quantityToMove;
                
                for (StockBatch batch : batchesToUse) {
                    int qtyFromBatch = Math.min(remainingQty, batch.getQuantityRemaining());
                    batch.reduceStock(qtyFromBatch);
                    stockBatchRepository.update(batch);
                    remainingQty -= qtyFromBatch;
                    
                    if (remainingQty == 0) break;
                }
                
                // Update shelf stock
                Optional<ShelfStock> shelfStockOpt = shelfStockRepository.findByItemCode(code);
                
                if (shelfStockOpt.isPresent()) {
                    ShelfStock shelfStock = shelfStockOpt.get();
                    shelfStock.addStock(quantityToMove);
                    shelfStockRepository.update(shelfStock);
                } else {
                    Optional<Item> itemOpt = itemRepository.findByCode(code);
                    if (itemOpt.isPresent()) {
                        ShelfStock newShelfStock = new ShelfStock(itemOpt.get(), quantityToMove);
                        shelfStockRepository.save(newShelfStock);
                    }
                }
                
                logger.info("Moved " + quantityToMove + " units to shelf for item: " + itemCode);
                return null;
                
            } catch (Exception e) {
                logger.error("Error moving stock to shelf", e);
                throw new RuntimeException("Failed to move stock to shelf", e);
            }
        });
    }
    
    /**
     * Strategy pattern - Select batches for shelving.
     * Priority: Expiring soon > Oldest batch (FIFO).
     */
    private List<StockBatch> selectBatchesForShelving(List<StockBatch> allBatches, 
                                                      int quantityNeeded) {
        List<StockBatch> result = new ArrayList<>();
        
        // Find batches with expiry dates
        List<StockBatch> withExpiry = allBatches.stream()
            .filter(b -> b.getExpiryDate() != null && b.getQuantityRemaining() > 0)
            .sorted(Comparator.comparing(StockBatch::getExpiryDate))
            .toList();
        
        // Find batches without expiry dates
        List<StockBatch> withoutExpiry = allBatches.stream()
            .filter(b -> b.getExpiryDate() == null && b.getQuantityRemaining() > 0)
            .sorted(Comparator.comparing(StockBatch::getPurchaseDate))
            .toList();
        
        int remaining = quantityNeeded;
        
        // First, check if any batch is expiring soon (within 30 days)
        LocalDate soonThreshold = LocalDate.now().plusDays(30);
        
        for (StockBatch batch : withExpiry) {
            if (batch.getExpiryDate().isBefore(soonThreshold)) {
                result.add(batch);
                remaining -= batch.getQuantityRemaining();
                if (remaining <= 0) return result;
            }
        }
        
        // Then use oldest batches without expiry
        for (StockBatch batch : withoutExpiry) {
            result.add(batch);
            remaining -= batch.getQuantityRemaining();
            if (remaining <= 0) return result;
        }
        
        // Finally, use remaining batches with expiry
        for (StockBatch batch : withExpiry) {
            if (!result.contains(batch)) {
                result.add(batch);
                remaining -= batch.getQuantityRemaining();
                if (remaining <= 0) return result;
            }
        }
        
        if (remaining > 0) {
            throw new IllegalArgumentException("Insufficient stock in batches");
        }
        
        return result;
    }
    
    /**
     * Get items below reorder level.
     */
    public List<ShelfStock> getItemsBelowReorderLevel() {
        try {
            return shelfStockRepository.findBelowReorderLevel();
        } catch (Exception e) {
            logger.error("Error getting items below reorder level", e);
            throw new RuntimeException("Failed to get reorder items", e);
        }
    }
    
    /**
     * Get all stock batches.
     */
    public List<StockBatch> getAllStockBatches() {
        try {
            return stockBatchRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting stock batches", e);
            throw new RuntimeException("Failed to get stock batches", e);
        }
    }
}