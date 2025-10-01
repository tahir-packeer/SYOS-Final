// File: src/main/java/org/syos/application/service/BillPrinter.java
package org.syos.application.service;

import org.syos.domain.entity.Bill;

/**
 * Service interface for printing bills.
 * Follows Interface Segregation Principle.
 */
public interface BillPrinter {
    void print(Bill bill);
    String formatBill(Bill bill);
}