// File: src/main/java/org/syos/application/repository/StockBatchRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.StockBatch;
import org.syos.domain.valueobject.ItemCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StockBatch entity.
 */
public interface StockBatchRepository {
    StockBatch save(StockBatch batch);
    Optional<StockBatch> findById(Long batchId);
    List<StockBatch> findByItemCode(ItemCode code);
    List<StockBatch> findByItemCodeOrderedByDate(ItemCode code);
    List<StockBatch> findAll();
    void update(StockBatch batch);
    void delete(Long batchId);
}
