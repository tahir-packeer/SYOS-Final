// File: src/main/java/org/syos/infrastructure/persistence/TransactionManager.java
package org.syos.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Centralized transaction management.
 * Ensures atomicity for multi-step database operations.
 */
public class TransactionManager {
    private final DBConnection dbConnection;
    
    public TransactionManager(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    /**
     * Execute operations within a transaction.
     * Automatically handles commit/rollback and connection release.
     */
    public <T> T executeInTransaction(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            T result = operation.apply(conn);
            
            conn.commit();
            return result;
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Transaction failed", e);
            
        } finally {
            if (conn != null) {
                dbConnection.releaseConnection(conn);
            }
        }
    }
    
    /**
     * Execute operations within a transaction without return value.
     */
    public void executeInTransactionVoid(java.util.function.Consumer<Connection> operation) {
        executeInTransaction(conn -> {
            operation.accept(conn);
            return null;
        });
    }
}