// File: src/main/java/org/syos/infrastructure/config/ConfigManager.java
package org.syos.infrastructure.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager - loads database configuration from properties file.
 * Singleton pattern for centralized configuration management.
 */
public class ConfigManager {
    private static volatile ConfigManager instance;
    private final Properties properties;
    
    private ConfigManager() {
        properties = new Properties();
        loadProperties();
    }
    
    /**
     * Get singleton instance using double-checked locking.
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load properties from application.properties file.
     */
    private void loadProperties() {
        // Try to load from classpath first
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                return;
            }
        } catch (IOException e) {
            System.err.println("Could not load properties from classpath");
        }
        
        // Try to load from file system
        try (FileInputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Could not load properties from file system");
            loadDefaultProperties();
        }
    }
    
    /**
     * Load default properties if file not found.
     */
    private void loadDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/syos_pos");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "9900@tahir");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.pool.size", "5");
    }
    
    public String getDbUrl() {
        return properties.getProperty("db.url");
    }
    
    public String getDbUsername() {
        return properties.getProperty("db.username");
    }
    
    public String getDbPassword() {
        return properties.getProperty("db.password");
    }
    
    public String getDbDriver() {
        return properties.getProperty("db.driver");
    }
    
    public int getDbPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.size", "5"));
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
