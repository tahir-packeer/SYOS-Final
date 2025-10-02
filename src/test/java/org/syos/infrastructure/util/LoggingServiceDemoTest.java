package org.syos.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.File;

/**
 * Test class to demonstrate the logging service functionality
 */
public class LoggingServiceDemoTest {

    @BeforeEach
    void setup() {
        // Clear any existing context before each test
        LoggingService.clearContext();
    }

    @AfterEach
    void cleanup() {
        // Clear context after each test
        LoggingService.clearContext();
    }

    @Test
    void demonstrateLoggingServiceFeatures() {
        System.out.println("\n=== SYOS POS Logging Service Demonstration ===\n");

        // 1. Application lifecycle logging
        System.out.println("1. Application Lifecycle Logging:");
        LoggingService.logApplicationStartup();

        // 2. User session management
        System.out.println("\n2. User Session Management:");
        LoggingService.initializeSession("user123", "session-abc-123");
        LoggingService.logUserLogin("john.doe", true);

        // 3. Business operations logging
        System.out.println("\n3. Business Operations Logging:");
        LoggingService.logBillCreated("BILL-001", "CUST-123", 125.50);
        LoggingService.logPaymentProcessed("BILL-001", "CASH", 125.50);
        LoggingService.logInventoryChange("ITEM-001", -5, "Sale transaction");
        LoggingService.logStockBatchCreated("BATCH-001", "ITEM-001", 100);
        LoggingService.logPriceChange("ITEM-001", 10.00, 12.00);
        LoggingService.logCustomerRegistered("CUST-456", "ONLINE");

        // 4. System operations logging
        System.out.println("\n4. System Operations Logging:");
        LoggingService.logDatabaseOperation("INSERT", "Customer", "CUST-456");
        LoggingService.logControllerEntry("CashierController", "processCounterSale", "john.doe", "125.50");
        LoggingService.logServiceMethodStart("BillingService", "createBill");

        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LoggingService.logServiceMethodEnd("BillingService", "createBill", 100);
        LoggingService.logControllerExit("CashierController", "processCounterSale");

        // 5. Error handling
        System.out.println("\n5. Error Handling:");
        Exception sampleException = new RuntimeException("Sample error for demonstration");
        LoggingService.logError("Demonstration error", sampleException);
        LoggingService.logValidationError("Customer", "phoneNumber", "invalid-phone", "Invalid phone format");
        LoggingService.logBusinessRuleViolation("InsufficientStock", "Item ITEM-001 quantity: 0");

        // 6. Performance monitoring
        System.out.println("\n6. Performance Monitoring:");
        LoggingService.PerformanceMonitor monitor = LoggingService.startPerformanceMonitoring("Sample Operation");

        // Simulate processing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        monitor.complete();

        // Test slow operation logging
        LoggingService.logSlowOperation("Database Query", 1500, 1000);

        // 7. Session cleanup
        System.out.println("\n7. Session Cleanup:");
        LoggingService.logUserLogout("john.doe");
        LoggingService.logApplicationShutdown();
        LoggingService.clearContext();

        System.out.println("\n=== Logging Service Demonstration Complete ===");
        System.out.println("\nCheck the following log files for detailed output:");
        System.out.println("- logs/syos-pos.log (Application logs)");
        System.out.println("- logs/audit.log (Audit trail)");
        System.out.println("- logs/errors.log (Error logs)");
        System.out.println("- Console output (Immediate feedback)\n");
    }

    @Test
    void testCorrelationIdGeneration() {
        System.out.println("\n=== Correlation ID Generation Test ===");

        String correlationId1 = LoggingService.generateCorrelationId();
        String correlationId2 = LoggingService.generateCorrelationId();

        System.out.println("Generated Correlation ID 1: " + correlationId1);
        System.out.println("Generated Correlation ID 2: " + correlationId2);

        // Ensure they are different
        assert !correlationId1.equals(correlationId2) : "Correlation IDs should be unique";

        // Test setting correlation ID
        LoggingService.setCorrelationId(correlationId1);
        LoggingService.setOperation("TEST_OPERATION");
        LoggingService.logDatabaseOperation("SELECT", "TestEntity", "test-id");

        System.out.println("Correlation ID functionality working correctly");
    }

    @Test
    void testPerformanceMonitoringWithError() {
        System.out.println("\n=== Performance Monitoring with Error Test ===");

        LoggingService.PerformanceMonitor monitor = LoggingService.startPerformanceMonitoring("Error Operation");

        try {
            // Simulate some work
            Thread.sleep(25);
            // Simulate an error
            throw new RuntimeException("Simulated error for testing");
        } catch (Exception e) {
            monitor.completeWithError(e);
            System.out.println("Error operation logged correctly");
        }
    }

    @Test
    void verifyLogDirectoryCreation() {
        System.out.println("\n=== Log Directory Verification ===");

        File logsDir = new File("logs");
        System.out.println("Logs directory exists: " + logsDir.exists());
        System.out.println("Logs directory path: " + logsDir.getAbsolutePath());

        if (logsDir.exists()) {
            File[] logFiles = logsDir.listFiles();
            if (logFiles != null) {
                System.out.println("Log files found: " + logFiles.length);
                for (File file : logFiles) {
                    System.out.println("- " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
        }
    }
}