// File: src/main/java/org/syos/application/repository/WebsiteInventoryRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.WebsiteInventory;
import org.syos.domain.valueobject.ItemCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WebsiteInventory entity.
 */
public interface WebsiteInventoryRepository {
    WebsiteInventory save(WebsiteInventory inventory);
    Optional<WebsiteInventory> findByItemCode(ItemCode code);
    List<WebsiteInventory> findAll();
    List<WebsiteInventory> findAvailableItems();
    void update(WebsiteInventory inventory);
    void delete(ItemCode code);
}
