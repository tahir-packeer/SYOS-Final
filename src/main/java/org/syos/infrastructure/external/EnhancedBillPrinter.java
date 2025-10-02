package org.syos.infrastructure.external;

import org.syos.application.service.BillPrinter;
import org.syos.domain.entity.Bill;
import org.syos.domain.entity.BillItem;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced BillPrinter that prints to console AND saves to file.
 */
public class EnhancedBillPrinter implements BillPrinter {

    private final String billsDirectory = "bills";

    public EnhancedBillPrinter() {
        // Create bills directory if it doesn't exist
        try {
            Path billsPath = Paths.get(billsDirectory);
            if (!Files.exists(billsPath)) {
                Files.createDirectories(billsPath);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create bills directory: " + e.getMessage());
        }
    }

    @Override
    public void print(Bill bill) {
        String formattedBill = formatBill(bill);

        // Always print to console first (most reliable)
        try {
            System.out.print(formattedBill);
            System.out.flush();
            System.out.print("✓ Bill printed to console\n");
            System.out.flush();
        } catch (Exception e) {
            System.err.print("Error printing to console: " + e.getMessage() + "\n");
            System.err.flush();
        }

        // Try to save to file (don't fail if this doesn't work)
        try {
            saveBillToFile(bill, formattedBill);
        } catch (Exception e) {
            System.err.print("Warning: Could not save bill to file: " + e.getMessage() + "\n");
            System.err.flush();
            // Don't throw exception - console printing succeeded
        }
    }

    @Override
    public String formatBill(Bill bill) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("        SYNEX OUTLET STORE           \n");
        sb.append("         Colombo, Sri Lanka          \n");
        sb.append("         Tel: +94-11-1234567         \n");
        sb.append("=====================================\n");
        sb.append("\n");
        sb.append("Bill Serial No: ").append(bill.getSerialNumber()).append("\n");
        sb.append("Date: ").append(bill.getDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Transaction Type: ").append(bill.getTransactionType().getDisplayName())
                .append("\n");

        if (bill.getCustomerName() != null && !bill.getCustomerName().equals("Walk-in Customer")) {
            sb.append("Customer: ").append(bill.getCustomerName()).append("\n");
        }

        sb.append("\n");
        sb.append("-------------------------------------\n");
        sb.append(String.format("%-20s %5s %10s %12s\n",
                "Item", "Qty", "Price", "Total"));
        sb.append("-------------------------------------\n");

        for (BillItem item : bill.getItems()) {
            sb.append(String.format("%-20s %5d %10s %12s\n",
                    truncate(item.getItem().getName(), 20),
                    item.getQuantity(),
                    item.getUnitPrice().toDisplayString(),
                    item.getTotalPrice().toDisplayString()));
        }

        sb.append("-------------------------------------\n");
        sb.append(String.format("%-36s %12s\n", "Subtotal:",
                bill.getSubtotal().toDisplayString()));

        if (!bill.getDiscount().isZero()) {
            sb.append(String.format("%-36s %12s\n", "Discount:",
                    bill.getDiscount().toDisplayString()));
        }

        sb.append(String.format("%-36s %12s\n", "TOTAL:",
                bill.getTotalAmount().toDisplayString()));
        sb.append("\n");

        sb.append("Payment Method: ").append(bill.getPaymentMethod().getDisplayName())
                .append("\n");

        if (bill.getCashTendered() != null) {
            sb.append(String.format("%-36s %12s\n", "Cash Tendered:",
                    bill.getCashTendered().toDisplayString()));
            sb.append(String.format("%-36s %12s\n", "Change:",
                    bill.getChangeAmount().toDisplayString()));
        }

        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("    Thank you for shopping with us!  \n");
        sb.append("=====================================\n");
        sb.append("\n");

        return sb.toString();
    }

    private void saveBillToFile(Bill bill, String formattedBill) {
        try {
            // Ensure directory exists
            Path billsPath = Paths.get(billsDirectory);
            if (!Files.exists(billsPath)) {
                Files.createDirectories(billsPath);
            }

            String dateString = bill.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String filename = String.format("BILL_%s_%s.txt",
                    bill.getSerialNumber(), dateString);
            Path filePath = Paths.get(billsDirectory, filename);

            // Write with explicit flushing and closing
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(formattedBill);
                writer.flush();
            }

            System.out.print("✓ Bill saved to file: " + filePath.getFileName() + "\n");
            System.out.flush();

        } catch (Exception e) {
            // Log but don't fail - file saving is optional
            System.err.print("Warning: Could not save bill to file: " + e.getMessage() + "\n");
            System.err.flush();
            throw new RuntimeException("File saving failed", e); // Re-throw as runtime exception
        }
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
