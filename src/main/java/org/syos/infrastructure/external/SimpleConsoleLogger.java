// File: src/main/java/org/syos/infrastructure/external/SimpleConsoleLogger.java
package org.syos.infrastructure.external;

import org.syos.application.service.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple console logger implementation.
 */
public class SimpleConsoleLogger implements Logger {
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void info(String message) {
        log("INFO", message);
    }
    
    @Override
    public void error(String message) {
        log("ERROR", message);
    }
    
    @Override
    public void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        throwable.printStackTrace();
    }
    
    @Override
    public void debug(String message) {
        log("DEBUG", message);
    }
    
    @Override
    public void warn(String message) {
        log("WARN", message);
    }
    
    private void log(String level, String message) {
        System.out.println(String.format("[%s] %s - %s", 
            LocalDateTime.now().format(formatter), level, message));
    }
}
