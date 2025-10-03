package org.syos.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple database connection manager using singleton pattern.
 * Clean, efficient implementation with minimal complexity.
 */
public class DBConnection {
    private static DBConnection instance;
    private static final Object lock = new Object();

    
    private final String url = "jdbc:mysql://localhost:3306/syos_pos";
    private final String username = "root";
    private final String password = "9900@tahir";

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get singleton instance with double-checked locking.
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (lock) {
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
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Clean up database connections to prevent memory leaks.
     */
    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }
}
