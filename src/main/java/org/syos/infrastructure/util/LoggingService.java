package org.syos.infrastructure.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Centralized logging service for SYOS POS system
 * Provides structured logging with correlation IDs and audit trails
 */
public class LoggingService {

    private static final Logger applicationLogger = LoggerFactory.getLogger(LoggingService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    // MDC keys for structured logging
    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String SESSION_ID = "sessionId";
    private static final String OPERATION = "operation";

    /**
     * Initialize logging context for a user session
     */
    public static void initializeSession(String userId, String sessionId) {
        MDC.put(USER_ID, userId);
        MDC.put(SESSION_ID, sessionId);
        MDC.put(CORRELATION_ID, generateCorrelationId());

        auditLogger.info("Session initialized - User: {}, Session: {}", userId, sessionId);
    }

    /**
     * Set correlation ID for request tracking
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
    }

    /**
     * Generate a new correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Set current operation context
     */
    public static void setOperation(String operation) {
        MDC.put(OPERATION, operation);
    }

    /**
     * Clear logging context
     */
    public static void clearContext() {
        MDC.clear();
    }

    // Business operation logging

    /**
     * Log user login attempt
     */
    public static void logUserLogin(String username, boolean success) {
        if (success) {
            auditLogger.info("User login successful - Username: {}", username);
        } else {
            auditLogger.warn("User login failed - Username: {}", username);
        }
    }

    /**
     * Log user logout
     */
    public static void logUserLogout(String username) {
        auditLogger.info("User logout - Username: {}", username);
    }

    /**
     * Log bill creation
     */
    public static void logBillCreated(String billId, String customerId, double amount) {
        auditLogger.info("Bill created - ID: {}, Customer: {}, Amount: ${}",
                billId, customerId, amount);
    }

    /**
     * Log payment processing
     */
    public static void logPaymentProcessed(String billId, String paymentMethod, double amount) {
        auditLogger.info("Payment processed - Bill: {}, Method: {}, Amount: ${}",
                billId, paymentMethod, amount);
    }

    /**
     * Log inventory changes
     */
    public static void logInventoryChange(String itemCode, int quantityChange, String reason) {
        auditLogger.info("Inventory change - Item: {}, Quantity: {}, Reason: {}",
                itemCode, quantityChange, reason);
    }

    /**
     * Log stock batch creation
     */
    public static void logStockBatchCreated(String batchId, String itemCode, int quantity) {
        auditLogger.info("Stock batch created - Batch: {}, Item: {}, Quantity: {}",
                batchId, itemCode, quantity);
    }

    /**
     * Log price changes
     */
    public static void logPriceChange(String itemCode, double oldPrice, double newPrice) {
        auditLogger.info("Price change - Item: {}, Old: ${}, New: ${}",
                itemCode, oldPrice, newPrice);
    }

    /**
     * Log customer registration
     */
    public static void logCustomerRegistered(String customerId, String customerType) {
        auditLogger.info("Customer registered - ID: {}, Type: {}", customerId, customerType);
    }

    /**
     * Log transaction start
     */
    public static void logTransactionStart(String transactionId, String type) {
        auditLogger.info("Transaction started - ID: {}, Type: {}", transactionId, type);
    }

    /**
     * Log transaction completion
     */
    public static void logTransactionComplete(String transactionId, String type) {
        auditLogger.info("Transaction completed - ID: {}, Type: {}", transactionId, type);
    }

    /**
     * Log bill printing status
     */
    public static void logBillPrintStatus(String billId, boolean success, String message) {
        if (success) {
            auditLogger.info("Bill printed successfully - ID: {}", billId);
        } else {
            auditLogger.warn("Bill printing failed - ID: {}, Message: {}", billId, message);
        }
    }

    // System operation logging

    /**
     * Log application startup
     */
    public static void logApplicationStartup() {
        applicationLogger.info("SYOS POS Application starting up...");
    }

    /**
     * Log application shutdown
     */
    public static void logApplicationShutdown() {
        applicationLogger.info("SYOS POS Application shutting down...");
    }

    /**
     * Log database operations
     */
    public static void logDatabaseOperation(String operation, String entity, String entityId) {
        applicationLogger.debug("Database {} - Entity: {}, ID: {}", operation, entity, entityId);
    }

    /**
     * Log controller entry
     */
    public static void logControllerEntry(String controller, String method, String... params) {
        applicationLogger.debug("Entering {}.{} with params: [{}]",
                controller, method, String.join(", ", params));
    }

    /**
     * Log controller exit
     */
    public static void logControllerExit(String controller, String method) {
        applicationLogger.debug("Exiting {}.{}", controller, method);
    }

    /**
     * Log service method execution
     */
    public static void logServiceMethodStart(String service, String method) {
        applicationLogger.debug("Starting {}.{}", service, method);
    }

    /**
     * Log service method completion
     */
    public static void logServiceMethodEnd(String service, String method, long executionTime) {
        applicationLogger.debug("Completed {}.{} in {}ms", service, method, executionTime);
    }

    // Error logging

    /**
     * Log application errors
     */
    public static void logError(String message, Throwable throwable) {
        applicationLogger.error(message, throwable);
    }

    /**
     * Log validation errors
     */
    public static void logValidationError(String entity, String field, String value, String error) {
        applicationLogger.warn("Validation error - Entity: {}, Field: {}, Value: {}, Error: {}",
                entity, field, value, error);
    }

    /**
     * Log business rule violations
     */
    public static void logBusinessRuleViolation(String rule, String context) {
        applicationLogger.warn("Business rule violation - Rule: {}, Context: {}", rule, context);
    }

    // Performance monitoring

    /**
     * Log slow operations
     */
    public static void logSlowOperation(String operation, long executionTime, long threshold) {
        if (executionTime > threshold) {
            applicationLogger.warn("Slow operation detected - Operation: {}, Time: {}ms, Threshold: {}ms",
                    operation, executionTime, threshold);
        }
    }

    /**
     * Create a performance monitoring wrapper
     */
    public static class PerformanceMonitor {
        private final String operation;
        private final long startTime;

        public PerformanceMonitor(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
            applicationLogger.debug("Starting operation: {}", operation);
        }

        public void complete() {
            long executionTime = System.currentTimeMillis() - startTime;
            applicationLogger.debug("Completed operation: {} in {}ms", operation, executionTime);

            // Log slow operations (threshold: 1 second)
            logSlowOperation(operation, executionTime, 1000);
        }

        public void completeWithError(Throwable error) {
            long executionTime = System.currentTimeMillis() - startTime;
            applicationLogger.error("Operation failed: {} after {}ms - Error: {}",
                    operation, executionTime, error.getMessage(), error);
        }
    }

    /**
     * Start performance monitoring for an operation
     */
    public static PerformanceMonitor startPerformanceMonitoring(String operation) {
        return new PerformanceMonitor(operation);
    }
}
