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
                    throw new IllegalArgumentException("No stock batches found for item code: " + itemCode + 
                        ". Please receive stock first or check if the item code exists.");
                }

                // Apply shelving strategy
                List<StockBatch> batchesToUse = selectBatchesForShelving(batches, quantityToMove);

                int remainingQty = quantityToMove;

                logger.info("Processing " + batchesToUse.size() + " batches for quantity: " + quantityToMove);
                for (StockBatch batch : batchesToUse) {
                    int qtyFromBatch = Math.min(remainingQty, batch.getQuantityRemaining());
                    logger.info("Taking " + qtyFromBatch + " units from batch " + batch.getBatchId());
                    batch.reduceStock(qtyFromBatch);
                    stockBatchRepository.update(batch);
                    logger.info("Batch " + batch.getBatchId() + " updated successfully");
                    remainingQty -= qtyFromBatch;

                    if (remainingQty == 0)
                        break;
                }
                logger.info("Batch processing completed");

                // Update shelf stock
                logger.info("Updating shelf stock for item: " + itemCode);
                Optional<ShelfStock> shelfStockOpt = shelfStockRepository.findByItemCode(code);

                if (shelfStockOpt.isPresent()) {
                    ShelfStock shelfStock = shelfStockOpt.get();
                    logger.info("Found existing shelf stock, current quantity: " + shelfStock.getQuantity());
                    shelfStock.addStock(quantityToMove);
                    logger.info("Updated shelf stock quantity to: " + shelfStock.getQuantity());
                    shelfStockRepository.update(shelfStock);
                    logger.info("Shelf stock update completed");
                } else {
                    logger.info("No existing shelf stock found, creating new entry");
                    Optional<Item> itemOpt = itemRepository.findByCode(code);
                    if (itemOpt.isPresent()) {
                        ShelfStock newShelfStock = new ShelfStock(itemOpt.get(), quantityToMove);
                        shelfStockRepository.save(newShelfStock);
                        logger.info("New shelf stock entry created");
                    } else {
                        throw new IllegalArgumentException("Item not found: " + itemCode);
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
     * Strategy pattern - Select batches for shelving according to CCCP1 requirement
     * 2b.
     * Primary rule: Use oldest batch (FIFO).
     * Exception rule: When expiry date of a newer batch is closer than the oldest
     * batch, choose the newer batch.
     */
    private List<StockBatch> selectBatchesForShelving(List<StockBatch> allBatches,
            int quantityNeeded) {
        List<StockBatch> result = new ArrayList<>();

        // Filter available batches with stock remaining, sorted by purchase date
        // (oldest first)
        List<StockBatch> availableBatches = allBatches.stream()
                .filter(b -> b.getQuantityRemaining() > 0)
                .sorted(Comparator.comparing(StockBatch::getPurchaseDate))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (availableBatches.isEmpty()) {
            throw new IllegalArgumentException("No stock batches with remaining quantity found. All batches may be empty or already moved to shelf.");
        }

        int remaining = quantityNeeded;

        while (remaining > 0 && !availableBatches.isEmpty()) {
            StockBatch selectedBatch = selectNextBatchForShelving(availableBatches);

            result.add(selectedBatch);
            remaining -= selectedBatch.getQuantityRemaining();

            // Remove the selected batch from available batches for next iteration
            availableBatches.remove(selectedBatch);
        }

        if (remaining > 0) {
            throw new IllegalArgumentException("Insufficient stock in batches. Need " + quantityNeeded +
                    " but only " + (quantityNeeded - remaining) + " available.");
        }

        return result;
    }

    /**
     * Selects the next batch for shelving according to CCCP1 requirement 2b.
     * Implementation of the business rule:
     * "The stock should be reduced from the oldest batch of items and put on the
     * shelf.
     * However, when the expiry date of another set is closer than the one in the
     * oldest
     * batch of items, the newer batch is chosen to stock the SYOS shelves."
     */
    private StockBatch selectNextBatchForShelving(List<StockBatch> availableBatches) {
        if (availableBatches.isEmpty()) {
            throw new IllegalArgumentException("No available batches to select from");
        }

        // Start with the oldest batch (FIFO principle)
        StockBatch oldestBatch = availableBatches.get(0);

        // If oldest batch has no expiry date, use it (no comparison possible)
        if (oldestBatch.getExpiryDate() == null) {
            logger.info("Selected oldest batch (ID: " + oldestBatch.getBatchId() +
                    ", purchased: " + oldestBatch.getPurchaseDate() +
                    ", no expiry date) - FIFO principle");
            return oldestBatch;
        }

        // Look through all batches to find one with closer expiry date than the oldest
        StockBatch selectedBatch = oldestBatch;
        LocalDate closestExpiryDate = oldestBatch.getExpiryDate();

        for (StockBatch batch : availableBatches) {
            // Skip batches without expiry dates
            if (batch.getExpiryDate() == null) {
                continue;
            }

            // If this batch expires sooner than our current selection, choose it
            if (batch.getExpiryDate().isBefore(closestExpiryDate)) {
                selectedBatch = batch;
                closestExpiryDate = batch.getExpiryDate();
            }
        }

        // Log the selection reason
        if (selectedBatch.equals(oldestBatch)) {
            logger.info("Selected oldest batch (ID: " + oldestBatch.getBatchId() +
                    ", purchased: " + oldestBatch.getPurchaseDate() +
                    ", expires: " + oldestBatch.getExpiryDate() + ") - FIFO principle");
        } else {
            logger.info("Selected batch with closer expiry (ID: " + selectedBatch.getBatchId() +
                    ", purchased: " + selectedBatch.getPurchaseDate() +
                    ", expires: " + selectedBatch.getExpiryDate() +
                    ") over oldest batch (ID: " + oldestBatch.getBatchId() +
                    ", purchased: " + oldestBatch.getPurchaseDate() +
                    ", expires: " + oldestBatch.getExpiryDate() + ") - Expiry priority rule");
        }

        return selectedBatch;
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

    /**
     * Get all shelf stock.
     */
    public List<ShelfStock> getAllShelfStock() {
        try {
            return shelfStockRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting shelf stock", e);
            throw new RuntimeException("Failed to get shelf stock", e);
        }
    }

    /**
     * Move stock from batches to website inventory.
     * Implements same FEFO (First Expired First Out) strategy with fallback to FIFO
     * as used for shelf stock.
     */
    public void moveToWebsite(String itemCode, int quantityToMove) {
        transactionManager.executeInTransaction(conn -> {
            try {
                ItemCode code = new ItemCode(itemCode);

                // Get all batches for this item, ordered
                List<StockBatch> batches = stockBatchRepository.findByItemCodeOrderedByDate(code);

                if (batches.isEmpty()) {
                    throw new IllegalArgumentException("No stock batches available for: " + itemCode);
                }

                // Apply same shelving strategy as shelf stock
                List<StockBatch> batchesToUse = selectBatchesForShelving(batches, quantityToMove);

                int remainingQty = quantityToMove;

                logger.info("Processing " + batchesToUse.size() + " batches for website quantity: " + quantityToMove);
                for (StockBatch batch : batchesToUse) {
                    int qtyFromBatch = Math.min(remainingQty, batch.getQuantityRemaining());
                    logger.info("Taking " + qtyFromBatch + " units from batch " + batch.getBatchId() + " for website");
                    batch.reduceStock(qtyFromBatch);
                    stockBatchRepository.update(batch);
                    logger.info("Batch " + batch.getBatchId() + " updated successfully for website move");
                    remainingQty -= qtyFromBatch;

                    if (remainingQty == 0)
                        break;
                }
                logger.info("Website batch processing completed");

                // Update website inventory
                logger.info("Updating website inventory for item: " + itemCode);
                Optional<org.syos.domain.entity.WebsiteInventory> websiteInventoryOpt = websiteInventoryRepository
                        .findByItemCode(code);

                if (websiteInventoryOpt.isPresent()) {
                    org.syos.domain.entity.WebsiteInventory websiteInventory = websiteInventoryOpt.get();
                    logger.info(
                            "Found existing website inventory, current quantity: " + websiteInventory.getQuantity());
                    websiteInventory.addStock(quantityToMove);
                    logger.info("Updated website inventory quantity to: " + websiteInventory.getQuantity());
                    websiteInventoryRepository.update(websiteInventory);
                    logger.info("Website inventory update completed");
                } else {
                    logger.info("No existing website inventory found, creating new entry");
                    Optional<Item> itemOpt = itemRepository.findByCode(code);
                    if (itemOpt.isPresent()) {
                        org.syos.domain.entity.WebsiteInventory newWebsiteInventory = new org.syos.domain.entity.WebsiteInventory(
                                itemOpt.get(), quantityToMove);
                        websiteInventoryRepository.save(newWebsiteInventory);
                        logger.info("New website inventory entry created");
                    } else {
                        throw new IllegalArgumentException("Item not found: " + itemCode);
                    }
                }

                logger.info("Moved " + quantityToMove + " units to website inventory for item: " + itemCode);
                return null;

            } catch (Exception e) {
                logger.error("Error moving stock to website", e);
                throw new RuntimeException("Failed to move stock to website", e);
            }
        });
    }

    /**
     * Get all website inventory.
     */
    public List<org.syos.domain.entity.WebsiteInventory> getAllWebsiteInventory() {
        try {
            return websiteInventoryRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting website inventory", e);
            throw new RuntimeException("Failed to get website inventory", e);
        }
    }

    /**
     * Get website inventory items that are available for online sales.
     */
    public List<org.syos.domain.entity.WebsiteInventory> getAvailableWebsiteItems() {
        try {
            return websiteInventoryRepository.findAvailableItems();
        } catch (Exception e) {
            logger.error("Error getting available website items", e);
            throw new RuntimeException("Failed to get available website items", e);
        }
    }
}
