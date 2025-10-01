// File: src/test/java/org/syos/usecase/StockShelvingStrategyTest.java
package org.syos.usecase;

import org.syos.domain.entity.Item;
import org.syos.domain.entity.StockBatch;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This shows how the shelving strategy works according to the business rule:
 * "The stock should be reduced from the oldest batch of items and put on the
 * shelf.
 * However, when the expiry date of another set is closer than the one in the
 * oldest
 * batch of items, the newer batch is chosen to stock the SYOS shelves."
 */
public class StockShelvingStrategyTest {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Stock Shelving Strategy Demonstration");
        System.out.println("=".repeat(80));

        // Create test scenarios
        demonstrateScenario1();
        demonstrateScenario2();
        demonstrateScenario3();
    }

    /**
     * Scenario 1: FIFO - Oldest batch selected when expiry dates are similar
     */
    private static void demonstrateScenario1() {
        System.out.println("\n--- SCENARIO 1: FIFO Principle ---");
        System.out.println("Testing when oldest batch should be selected");

        List<StockBatch> batches = new ArrayList<>();

        // Create mock item
        Item testItem = createMockItem("ITEM001", "Test Product");

        // Batch 1: Older, expires later
        StockBatch batch1 = createMockBatch(1L, testItem, 50, 50,
                LocalDate.of(2025, 9, 1), // Purchase date (older)
                LocalDate.of(2025, 12, 1)); // Expiry date (later)

        // Batch 2: Newer, expires later
        StockBatch batch2 = createMockBatch(2L, testItem, 30, 30,
                LocalDate.of(2025, 9, 15), // Purchase date (newer)
                LocalDate.of(2025, 12, 15)); // Expiry date (later)

        batches.add(batch1);
        batches.add(batch2);

        System.out.println("Available batches:");
        printBatchInfo(batch1);
        printBatchInfo(batch2);

        System.out.println("\nExpected: Batch 1 should be selected (FIFO principle - older batch)");
        System.out.println("Reason: Both batches expire around the same time, so oldest batch is preferred");
    }

    /**
     * Scenario 2: Expiry Priority - Newer batch with closer expiry selected
     */
    private static void demonstrateScenario2() {
        System.out.println("\n--- SCENARIO 2: Expiry Date Priority ---");
        System.out.println("Testing when newer batch with closer expiry should be selected");

        List<StockBatch> batches = new ArrayList<>();

        // Create mock item
        Item testItem = createMockItem("ITEM002", "Perishable Product");

        // Batch 1: Older, expires much later
        StockBatch batch1 = createMockBatch(3L, testItem, 40, 40,
                LocalDate.of(2025, 8, 1), // Purchase date (older)
                LocalDate.of(2025, 12, 31)); // Expiry date (much later)

        // Batch 2: Newer, but expires much sooner
        StockBatch batch2 = createMockBatch(4L, testItem, 25, 25,
                LocalDate.of(2025, 9, 20), // Purchase date (newer)
                LocalDate.of(2025, 10, 15)); // Expiry date (much sooner)

        batches.add(batch1);
        batches.add(batch2);

        System.out.println("Available batches:");
        printBatchInfo(batch1);
        printBatchInfo(batch2);

        System.out.println("\nExpected: Batch 2 should be selected (Expiry priority rule)");
        System.out.println("Reason: Even though Batch 1 is older, Batch 2 expires much sooner");
    }

    /**
     * Scenario 3: No Expiry Date - Always use FIFO
     */
    private static void demonstrateScenario3() {
        System.out.println("\n--- SCENARIO 3: No Expiry Date ---");
        System.out.println("Testing when batches have no expiry dates");

        List<StockBatch> batches = new ArrayList<>();

        // Create mock item
        Item testItem = createMockItem("ITEM003", "Non-Perishable Product");

        // Batch 1: Older, no expiry
        StockBatch batch1 = createMockBatch(5L, testItem, 60, 60,
                LocalDate.of(2025, 7, 15), // Purchase date (older)
                null); // No expiry date

        // Batch 2: Newer, no expiry
        StockBatch batch2 = createMockBatch(6L, testItem, 35, 35,
                LocalDate.of(2025, 8, 10), // Purchase date (newer)
                null); // No expiry date

        batches.add(batch1);
        batches.add(batch2);

        System.out.println("Available batches:");
        printBatchInfo(batch1);
        printBatchInfo(batch2);

        System.out.println("\nExpected: Batch 1 should be selected (FIFO principle)");
        System.out.println("Reason: No expiry dates to compare, so oldest batch is used");

        System.out.println("\n" + "=".repeat(80));

        System.out.println("=".repeat(80));
    }

    // Helper methods for creating mock objects
    private static Item createMockItem(String code, String name) {
        ItemCode itemCode = new ItemCode(code);
        Money unitPrice = Money.of(10.0);
        return new Item(name, itemCode, unitPrice, BigDecimal.ZERO, 5);
    }

    private static StockBatch createMockBatch(Long id, Item item, int quantityReceived,
            int quantityRemaining, LocalDate purchaseDate,
            LocalDate expiryDate) {
        StockBatch batch = new StockBatch(item, quantityReceived, purchaseDate, expiryDate);
        batch.setBatchId(id);
        batch.setQuantityRemaining(quantityRemaining);
        return batch;
    }

    private static void printBatchInfo(StockBatch batch) {
        System.out.printf("  Batch ID: %d | Purchase: %s | Expiry: %s | Qty: %d%n",
                batch.getBatchId(),
                batch.getPurchaseDate(),
                batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : "No expiry",
                batch.getQuantityRemaining());
    }
}