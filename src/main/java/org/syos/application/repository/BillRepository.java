// File: src/main/java/org/syos/application/repository/BillRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.Bill;
import org.syos.domain.enums.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bill entity.
 */
public interface BillRepository {
    Bill save(Bill bill);
    Optional<Bill> findById(Long billId);
    Optional<Bill> findBySerialNumber(String serialNumber);
    List<Bill> findAll();
    List<Bill> findByDate(LocalDate date);
    List<Bill> findByTransactionType(TransactionType type);
    List<Bill> findByDateAndType(LocalDate date, TransactionType type);
    long getNextSerialNumber();
}