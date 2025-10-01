// File: src/main/java/org/syos/infrastructure/persistence/DBConnection.java
package org.syos.infrastructure.persistence;

import org.syos.infrastructure.config.ConfigManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe singleton connection pool using LinkedBlockingQueue.
 * Manages a fixed-size pool of reusable database connections.
 * 
 * Design Patterns:
 * - Singleton: Ensures single instance
 * - Object Pool: Reuses connections efficiently
 * 
 * Thread Safety:
 * - LinkedBlockingQueue provides built-in thread safety
 * - Double-checked locking for singleton initialization
 */
public class DBConnection {
    private static volatile DBConnection instance;
    private final LinkedBlockingQueue<Connection> connectionPool;
    private final int poolSize;
    private final ConfigManager config;
    
    /**
     * Private constructor - use getInstance().
     */
    private DBConnection() {
        this.config = ConfigManager.getInstance();
        this.poolSize = config.getDbPoolSize();
        this.connectionPool = new LinkedBlockingQueue<>(poolSize);
        
        try {
            // Load JDBC driver
            Class.forName(config.getDbDriver());
            
            // Initialize connection pool
            for (int i = 0; i < poolSize; i++) {
                Connection conn = createNewConnection();
                connectionPool.offer(conn);
            }
            
            System.out.println("Database connection pool initialized with " + 
                             poolSize + " connections");
                             
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }
    
    /**
     * Get singleton instance with double-checked locking.
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Create a new database connection.
     */
    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
            config.getDbUrl(),
            config.getDbUsername(),
            config.getDbPassword()
        );
    }
    
    /**
     * Get a connection from the pool.
     * Blocks if no connections are available until one is released.
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.take(); // Blocks until available
            
            // Validate connection is still valid
            if (conn.isClosed() || !conn.isValid(2)) {
                conn = createNewConnection();
            }
            
            return conn;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    /**
     * Release a connection back to the pool.
     * MUST be called after using a connection to prevent leaks.
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                // Reset connection state
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                
                // Return to pool
                connectionPool.offer(conn);
                
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
                // Try to create a new connection to replace the faulty one
                try {
                    Connection newConn = createNewConnection();
                    connectionPool.offer(newConn);
                } catch (SQLException ex) {
                    System.err.println("Error creating replacement connection: " + 
                                     ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Close all connections in the pool.
     * Should be called during application shutdown.
     */
    public void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            try {
                Connection conn = connectionPool.poll();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        System.out.println("All database connections closed");
    }
    
    /**
     * Get current pool size.
     */
    public int getAvailableConnections() {
        return connectionPool.size();
    }
}