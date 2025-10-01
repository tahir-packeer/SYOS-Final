// File: src/main/java/org/syos/application/repository/ShelfStockRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.ShelfStock;
import org.syos.domain.valueobject.ItemCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShelfStock entity.
 */
public interface ShelfStockRepository {
    ShelfStock save(ShelfStock stock);
    Optional<ShelfStock> findByItemCode(ItemCode code);
    List<ShelfStock> findAll();
    List<ShelfStock> findBelowReorderLevel();
    void update(ShelfStock stock);
    void delete(ItemCode code);
}
