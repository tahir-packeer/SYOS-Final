// File: src/main/java/org/syos/infrastructure/external/UUIDSerialNumberGenerator.java
package org.syos.infrastructure.external;

import org.syos.application.service.SerialNumberGenerator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates bill serial numbers in format: YYYYMMDD-NNNNNN
 */
public class UUIDSerialNumberGenerator implements SerialNumberGenerator {
    
    @Override
    public String generateSerialNumber(long sequenceNumber) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("%s-%06d", datePrefix, sequenceNumber);
    }
}