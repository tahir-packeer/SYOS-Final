// File: src/main/java/org/syos/view/ManagerView.java
package org.syos.view;

import org.syos.controller.ManagerController;
import java.util.Scanner;

/**
 * Console view for manager operations.
 */
public class ManagerView extends ConsoleView {
    private final ManagerController controller;

    public ManagerView(Scanner scanner, ManagerController controller) {
        super(scanner);
        this.controller = controller;
    }

    @Override
    public void display() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Item Management",
                    "Stock Management",
                    "Reports",
                    "Customer Management",
                    "Logout"
            };

            int choice = displayMenu("MANAGER MENU", options);

            switch (choice) {
                case 1:
                    itemManagementMenu();
                    break;
                case 2:
                    stockManagementMenu();
                    break;
                case 3:
                    reportsMenu();
                    break;
                case 4:
                    customerManagementMenu();
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
     * Item management submenu.
     */
    private void itemManagementMenu() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Add New Item",
                    "View All Items",
                    "Update Item",
                    "Delete Item",
                    "Search Item"
            };

            int choice = displayMenu("ITEM MANAGEMENT", options);

            switch (choice) {
                case 1:
                    addNewItem();
                    break;
                case 2:
                    viewAllItems();
                    break;
                case 3:
                    updateItem();
                    break;
                case 4:
                    deleteItem();
                    break;
                case 5:
                    searchItem();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    displayError("Invalid choice.");
                    break;
            }
        }
    }

    /**
     * Stock management submenu.
     */
    private void stockManagementMenu() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Receive Stock",
                    "Move to Shelf",
                    "Move to Website Inventory",
                    "View Stock Batches",
                    "View Shelf Stock",
                    "View Website Inventory"
            };

            int choice = displayMenu("STOCK MANAGEMENT", options);

            switch (choice) {
                case 1:
                    receiveStock();
                    break;
                case 2:
                    moveToShelf();
                    break;
                case 3:
                    moveToWebsite();
                    break;
                case 4:
                    viewStockBatches();
                    break;
                case 5:
                    viewShelfStock();
                    break;
                case 6:
                    viewWebsiteInventory();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    displayError("Invalid choice.");
                    break;
            }
        }
    }

    /**
     * Reports submenu.
     */
    private void reportsMenu() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Daily Sales Report",
                    "Stock Report",
                    "Reorder Level Report",
                    "Bill Report",
                    "Counter Sales Report",
                    "Online Sales Report"
            };

            int choice = displayMenu("REPORTS", options);

            switch (choice) {
                case 1:
                    dailySalesReport();
                    break;
                case 2:
                    stockReport();
                    break;
                case 3:
                    reorderReport();
                    break;
                case 4:
                    billReport();
                    break;
                case 5:
                    counterSalesReport();
                    break;
                case 6:
                    onlineSalesReport();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    displayError("Invalid choice.");
                    break;
            }
        }
    }

    /**
     * Customer management submenu.
     */
    private void customerManagementMenu() {
        boolean running = true;

        while (running) {
            String[] options = {
                    "Register Customer",
                    "View All Customers",
                    "Search Customer"
            };

            int choice = displayMenu("CUSTOMER MANAGEMENT", options);

            switch (choice) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    viewAllCustomers();
                    break;
                case 3:
                    searchCustomer();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    displayError("Invalid choice.");
                    break;
            }
        }
    }

    // Item Management Methods

    private void addNewItem() {
        displayHeader("ADD NEW ITEM");

        try {
            String name = getInput("Item Name: ");
            String code = getInput("Item Code: ");
            double price = Double.parseDouble(getInput("Unit Price (Rs): "));
            double discount = Double.parseDouble(getInput("Discount (%): "));
            int reorderLevel = getIntInput("Reorder Level: ");

            boolean success = controller.addNewItem(name, code, price, discount, reorderLevel);

            if (success) {
                displaySuccess("Item added successfully!");
            } else {
                displayError("Failed to add item.");
            }
        } catch (Exception e) {
            displayError("Error: " + e.getMessage());
        }

        pause();
    }

    private void viewAllItems() {
        displayHeader("ALL ITEMS");
        controller.displayAllItems();
        pause();
    }

    private void updateItem() {
        displayHeader("UPDATE ITEM");
        displayError("Feature not implemented in CCCP1");
        pause();
    }

    private void deleteItem() {
        displayHeader("DELETE ITEM");
        displayError("Feature not implemented in CCCP1");
        pause();
    }

    private void searchItem() {
        displayHeader("SEARCH ITEM");
        String code = getInput("Enter Item Code: ");
        controller.searchItemByCode(code);
        pause();
    }

    // Stock Management Methods

    private void receiveStock() {
        displayHeader("RECEIVE STOCK");

        try {
            String itemCode = getInput("Item Code: ");
            int quantity = getIntInput("Quantity: ");
            String purchaseDate = getInput("Purchase Date (YYYY-MM-DD): ");
            String expiryDate = getInput("Expiry Date (YYYY-MM-DD, optional): ");

            boolean success = controller.receiveStock(itemCode, quantity, purchaseDate,
                    expiryDate.isEmpty() ? null : expiryDate);

            if (success) {
                displaySuccess("Stock received successfully!");
            } else {
                displayError("Failed to receive stock.");
            }
        } catch (Exception e) {
            displayError("Error: " + e.getMessage());
        }

        pause();
    }

    private void moveToShelf() {
        displayHeader("MOVE STOCK TO SHELF");

        try {
            // First show available stock batches
            System.out.println("Available Stock Batches:");
            controller.displayStockBatches();
            System.out.println();
            
            String itemCode = getInput("Item Code: ");
            int quantity = getIntInput("Quantity to Move: ");

            boolean success = controller.moveToShelf(itemCode, quantity);

            if (success) {
                displaySuccess("Stock moved to shelf successfully!");
            } else {
                displayError("Failed to move stock.");
            }
        } catch (Exception e) {
            displayError("Error: " + e.getMessage());
        }

        pause();
    }

    private void moveToWebsite() {
        displayHeader("MOVE STOCK TO WEBSITE");

        String itemCode = getInput("Enter Item Code: ");
        String quantityStr = getInput("Enter Quantity to Move: ");

        try {
            int quantity = Integer.parseInt(quantityStr);

            if (controller.moveToWebsite(itemCode, quantity)) {
                displaySuccess("Successfully moved " + quantity + " units of " + itemCode + " to website inventory");
            } else {
                displayError("Failed to move stock to website");
            }
        } catch (NumberFormatException e) {
            displayError("Invalid quantity format");
        }

        pause();
    }

    private void viewStockBatches() {
        displayHeader("STOCK BATCHES");
        controller.displayStockBatches();
        pause();
    }

    private void viewShelfStock() {
        displayHeader("SHELF STOCK");
        controller.displayShelfStock();
        pause();
    }

    private void viewWebsiteInventory() {
        displayHeader("WEBSITE INVENTORY");
        controller.displayWebsiteInventory();
        pause();
    }

    // Report Methods

    private void dailySalesReport() {
        displayHeader("DAILY SALES REPORT");
        String date = getInput("Enter Date (YYYY-MM-DD, or press Enter for today): ");
        controller.displayDailySalesReport(date.isEmpty() ? null : date);
        pause();
    }

    private void stockReport() {
        displayHeader("STOCK REPORT");
        controller.displayStockReport();
        pause();
    }

    private void reorderReport() {
        displayHeader("REORDER LEVEL REPORT");
        controller.displayReorderReport();
        pause();
    }

    private void billReport() {
        displayHeader("BILL REPORT");
        controller.displayBillReport();
        pause();
    }

    private void counterSalesReport() {
        displayHeader("COUNTER SALES REPORT");
        String date = getInput("Enter Date (YYYY-MM-DD, or press Enter for today): ");
        controller.displayCounterSalesReport(date.isEmpty() ? null : date);
        pause();
    }

    private void onlineSalesReport() {
        displayHeader("ONLINE SALES REPORT");
        String date = getInput("Enter Date (YYYY-MM-DD, or press Enter for today): ");
        controller.displayOnlineSalesReport(date.isEmpty() ? null : date);
        pause();
    }

    // Customer Management Methods

    private void registerCustomer() {
        displayHeader("REGISTER CUSTOMER");

        try {
            String name = getInput("Customer Name: ");
            String phone = getInput("Phone Number: ");

            boolean success = controller.registerCustomer(name, phone);

            if (success) {
                displaySuccess("Customer registered successfully!");
            } else {
                displayError("Failed to register customer.");
            }
        } catch (Exception e) {
            displayError("Error: " + e.getMessage());
        }

        pause();
    }

    private void viewAllCustomers() {
        displayHeader("ALL CUSTOMERS");
        controller.displayAllCustomers();
        pause();
    }

    private void searchCustomer() {
        displayHeader("SEARCH CUSTOMER");
        String phone = getInput("Enter Phone Number: ");
        controller.searchCustomerByPhone(phone);
        pause();
    }
}
