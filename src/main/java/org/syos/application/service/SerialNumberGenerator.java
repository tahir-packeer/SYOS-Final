// File: src/main/java/org/syos/application/service/SerialNumberGenerator.java
package org.syos.application.service;

/**
 * Service interface for generating bill serial numbers.
 */
public interface SerialNumberGenerator {
    String generateSerialNumber(long sequenceNumber);
}
