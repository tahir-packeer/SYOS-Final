// File: src/main/java/org/syos/view/CashierView.java
package org.syos.view;

import org.syos.controller.CashierController;
import java.util.Scanner;

/**
 * Console view for cashier operations.
 * Minimal logic - delegates to controller.
 */
public class CashierView extends ConsoleView {
    private final CashierController controller;

    public CashierView(Scanner scanner, CashierController controller) {
        super(scanner);
        this.controller = controller;
    }

    @Override
    public void display() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Process Sale",
                    "View Items",
                    "Check Stock",
                    "View Today's Sales Report",
                    "Logout"
            };

            int choice = displayMenu("CASHIER MENU", options);

            switch (choice) {
                case 1:
                    processSale();
                    break;
                case 2:
                    viewItems();
                    break;
                case 3:
                    checkStock();
                    break;
                case 4:
                    viewTodaysSales();
                    break;
                case 5:
                case 0:
                    running = false;
                    break;
                default:
                    displayError("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    /**
     * Process a sale transaction.
     */
    private void processSale() {
        displayHeader("PROCESS SALE");

        try {
            // Get customer name
            String customerName = getInput("Customer Name (optional, press Enter to skip): ");
            if (customerName.isEmpty()) {
                customerName = "Walk-in Customer";
            }

            // Get items
            java.util.List<org.syos.application.usecase.BillingAppService.OrderItem> orderItems = new java.util.ArrayList<>();

            boolean addingItems = true;
            while (addingItems) {
                String itemCode = getInput("\nEnter Item Code (or 'done' to finish): ");

                if (itemCode.equalsIgnoreCase("done")) {
                    if (orderItems.isEmpty()) {
                        displayError("Must add at least one item");
                        continue;
                    }
                    addingItems = false;
                } else {
                    int quantity = getIntInput("Enter Quantity: ");

                    if (quantity <= 0) {
                        displayError("Quantity must be positive");
                        continue;
                    }

                    orderItems.add(new org.syos.application.usecase.BillingAppService.OrderItem(
                            itemCode, quantity));
                    displaySuccess("Added to cart");
                }
            }

            // Get discount
            System.out.println("\nTotal items in cart: " + orderItems.size());
            System.out.println("\n--- DISCOUNT ---");
            String discountInput = getInput("Discount Amount (Rs) [Press Enter for no discount]: ");
            double discountAmount = 0.0;
            if (!discountInput.isEmpty()) {
                try {
                    discountAmount = Double.parseDouble(discountInput);
                    if (discountAmount < 0) {
                        displayError("Discount amount cannot be negative. Setting to 0.");
                        discountAmount = 0.0;
                    }
                } catch (NumberFormatException e) {
                    displayError("Invalid discount amount. Setting to 0.");
                    discountAmount = 0.0;
                }
            }

            // Get payment
            double cashAmount = Double.parseDouble(getInput("Cash Tendered (Rs): "));

            // Process through controller
            boolean success = controller.processCounterSale(orderItems, customerName, cashAmount, discountAmount);

            if (success) {
                displaySuccess("Sale completed successfully!");
            } else {
                displayError("Sale failed. Please try again.");
            }

        } catch (Exception e) {
            displayError("Error processing sale: " + e.getMessage());
        }

        pause();
    }

    /**
     * View all items.
     */
    private void viewItems() {
        displayHeader("ITEM LIST");
        controller.displayAllItems();
        pause();
    }

    /**
     * Check stock levels.
     */
    private void checkStock() {
        displayHeader("STOCK CHECK");
        String itemCode = getInput("Enter Item Code: ");
        controller.checkItemStock(itemCode);
        pause();
    }

    /**
     * View today's sales report.
     */
    private void viewTodaysSales() {
        displayHeader("TODAY'S SALES REPORT");
        controller.displayTodaysSalesReport();
        pause();
    }
}