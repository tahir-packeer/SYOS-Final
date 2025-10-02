package org.syos.view;

import org.syos.application.usecase.BillingAppService;
import org.syos.controller.CashierController;
import org.syos.domain.entity.BillItem;
import org.syos.domain.entity.Customer;
import java.util.Optional;
import java.util.Scanner;

/**
 * Enhanced console view for cashier operations with improved checkout flow.
 */
public class EnhancedCashierView extends ConsoleView {
    private final CashierController controller;

    public EnhancedCashierView(Scanner scanner, CashierController controller) {
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
                    "Today's Sales",
                    "Logout"
            };

            int choice = displayMenu("CASHIER MENU", options);
            switch (choice) {
                case 1:
                    processEnhancedSale();
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
     * Simple sale process with customer registration and bill preview.
     */
    private void processEnhancedSale() {
        displayHeader("PROCESS SALE");

        try {
            // Step 1: Customer Information
            System.out.println("\n--- Customer Information ---");

            String customerName = getInput("Customer Name (Enter for walk-in): ");
            if (customerName.trim().isEmpty()) {
                customerName = "Walk-in Customer";
            }

            String phoneNumber = "";
            Customer registeredCustomer = null;

            if (!customerName.equals("Walk-in Customer")) {
                phoneNumber = getInput("Phone Number: ");

                if (!phoneNumber.trim().isEmpty()) {
                    System.out.println("Checking customer...");

                    Optional<Customer> customerOpt = controller.findOrRegisterCustomer(customerName, phoneNumber);
                    if (customerOpt.isPresent()) {
                        registeredCustomer = customerOpt.get();
                        if (registeredCustomer.getName().equals(customerName)) {
                            displaySuccess("Existing customer: " + registeredCustomer.getName());
                        } else {
                            displaySuccess("New customer registered: " + customerName);
                        }
                    }
                }
            }

            // Step 2: Add Items to Cart
            System.out.println("\n--- Add Items ---");

            java.util.List<BillingAppService.OrderItem> orderItems = new java.util.ArrayList<>();

            boolean addingItems = true;
            while (addingItems) {
                System.out.println("\nCart: " + orderItems.size() + " items");
                String itemCode = getInput("Item Code (or 'done' to finish, 'list' for items): ");

                if (itemCode.equalsIgnoreCase("done")) {
                    if (orderItems.isEmpty()) {
                        displayError("❌ Must add at least one item");
                        continue;
                    }
                    addingItems = false;
                } else if (itemCode.equalsIgnoreCase("list")) {
                    controller.displayAllItems();
                } else {
                    // Check if item exists first
                    controller.checkItemStock(itemCode);

                    String continueAdd = getInput("Add to cart? (y/n): ");
                    if (continueAdd.toLowerCase().startsWith("y")) {
                        int quantity = getIntInput("Quantity: ");

                        if (quantity <= 0) {
                            displayError("Quantity must be positive");
                            continue;
                        }

                        orderItems.add(new BillingAppService.OrderItem(itemCode, quantity));
                        displaySuccess("Added " + quantity + " x " + itemCode);
                    }
                }
            }

            // Step 3: Apply Discount
            System.out.println("\n--- Discount ---");

            String discountInput = getInput("Discount Amount (Rs) [Enter for none]: ");
            double discountAmount = 0.0;
            if (!discountInput.isEmpty()) {
                try {
                    discountAmount = Double.parseDouble(discountInput);
                    if (discountAmount < 0) {
                        displayError("Discount cannot be negative. Setting to 0.");
                        discountAmount = 0.0;
                    }
                } catch (NumberFormatException e) {
                    displayError("Invalid amount. Setting to 0.");
                    discountAmount = 0.0;
                }
            }

            // Step 4: Bill Preview
            System.out.println("\n--- Bill Preview ---");

            BillingAppService.BillPreview preview = controller.previewBill(orderItems, customerName, discountAmount);
            if (preview == null) {
                displayError("Error generating preview");
                return;
            }

            displayBillPreview(preview);

            String confirmSale = getInput("\nConfirm sale? (y/n): ");
            if (!confirmSale.toLowerCase().startsWith("y")) {
                System.out.println("Sale cancelled.");
                return;
            }

            // Step 5: Payment
            System.out.println("\n--- Payment ---");

            System.out.printf("Total: %s\n", preview.getTotal().toDisplayString());

            double cashAmount;
            while (true) {
                try {
                    cashAmount = Double.parseDouble(getInput("Cash Tendered (Rs): "));
                    if (cashAmount >= preview.getTotal().getAmount().doubleValue()) {
                        break;
                    } else {
                        displayError("Insufficient cash. Need: " + preview.getTotal().toDisplayString());
                    }
                } catch (NumberFormatException e) {
                    displayError("Invalid amount. Enter a valid number.");
                }
            }

            // Step 6: Process Payment
            System.out.println("\nProcessing payment...");

            boolean success = controller.processCounterSale(orderItems, customerName, cashAmount, discountAmount);

            if (success) {
                displaySuccess("\nSALE COMPLETED!");
                double change = cashAmount - preview.getTotal().getAmount().doubleValue();
                if (change > 0) {
                    System.out.printf("Change: Rs %.2f\n", change);
                }
                System.out.println("Bill printed and saved.");

                if (registeredCustomer != null) {
                    System.out.println("Customer: " + registeredCustomer.getName() + " ("
                            + registeredCustomer.getPhone() + ")");
                }
            } else {
                displayError("Sale failed. Try again.");
            }

        } catch (Exception e) {
            displayError("Error processing sale: " + e.getMessage());
        }

        pause();
    }

    /**
     * Display bill preview with simple layout.
     */
    private void displayBillPreview(BillingAppService.BillPreview preview) {
        System.out.println();
        System.out.println("=== BILL PREVIEW ===");
        System.out.println("Customer: " + preview.getCustomerName());
        System.out.println();
        System.out.printf("%-20s %6s %10s %12s\n", "Item", "Qty", "Price", "Total");
        System.out.println("----------------------------------------------------");

        for (BillItem item : preview.getItems()) {
            System.out.printf("%-20s %6d %10s %12s\n",
                    truncate(item.getItem().getName(), 20),
                    item.getQuantity(),
                    item.getUnitPrice().toDisplayString(),
                    item.getTotalPrice().toDisplayString());
        }

        System.out.println("----------------------------------------------------");
        System.out.printf("%-40s %12s\n", "Subtotal:", preview.getSubtotal().toDisplayString());

        if (!preview.getDiscount().isZero()) {
            System.out.printf("%-40s %12s\n", "Discount:", preview.getDiscount().toDisplayString());
        }

        System.out.printf("%-40s %12s\n", "TOTAL:", preview.getTotal().toDisplayString());
        System.out.println("====================================================");
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

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
