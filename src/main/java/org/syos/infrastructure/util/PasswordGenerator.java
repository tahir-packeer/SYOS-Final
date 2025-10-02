package org.syos.infrastructure.util;

/**
 * Utility to generate hashed passwords for database initialization.
 * Run this to get properly hashed passwords for schema.sql.
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        PasswordHasher hasher = new PasswordHasher();

        // Generate hashes for common test passwords
        System.out.println("=== Password Hashes for Database Init ===");
        System.out.println("admin123: " + hasher.hash("admin123"));
        System.out.println("cashier123: " + hasher.hash("cashier123"));
        System.out.println("manager123: " + hasher.hash("manager123"));
        System.out.println();

        // Verify the hashes work
        String adminHash = hasher.hash("admin123");
        System.out.println("Verification test:");
        System.out.println("admin123 verifies: " + hasher.verify("admin123", adminHash));
        System.out.println("wrong password verifies: " + hasher.verify("wrongpass", adminHash));

        //verify for cashier
        String cashierHash = hasher.hash("cashier123");
        System.out.println("cashier123 verifies: " + hasher.verify("cashier123", cashierHash));
        System.out.println("wrong password verifies: " + hasher.verify("wrongpass", cashierHash));

        //verify for manager
        String managerHash = hasher.hash("manager123");
        System.out.println("manager123 verifies: " + hasher.verify("manager123", managerHash));
        System.out.println("wrong password verifies: " + hasher.verify("wrongpass", managerHash));
    }
}
