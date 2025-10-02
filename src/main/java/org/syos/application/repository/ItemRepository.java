// File: src/main/java/org/syos/application/repository/ItemRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.Item;
import org.syos.domain.valueobject.ItemCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Item entity.
 */
public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(Long itemId);
    Optional<Item> findByCode(ItemCode code);
    List<Item> findAll();
    List<Item> searchByName(String name);
    void update(Item item);
    void delete(Long itemId);
    boolean existsByCode(ItemCode code);
}
