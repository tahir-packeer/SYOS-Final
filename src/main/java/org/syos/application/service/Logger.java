// File: src/main/java/org/syos/application/service/Logger.java
package org.syos.application.service;

/**
 * Service interface for logging.
 */
public interface Logger {
    void info(String message);
    void error(String message);
    void error(String message, Throwable throwable);
    void debug(String message);
    void warn(String message);
}