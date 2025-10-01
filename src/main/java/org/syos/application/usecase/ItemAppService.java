// File: src/main/java/org/syos/application/usecase/ItemAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.ItemRepository;
import org.syos.application.service.Logger;
import org.syos.domain.entity.Item;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Application service for item management.
 */
public class ItemAppService {
    private final ItemRepository itemRepository;
    private final Logger logger;
    
    public ItemAppService(ItemRepository itemRepository, Logger logger) {
        this.itemRepository = itemRepository;
        this.logger = logger;
    }
    
    /**
     * Create new item.
     */
    public Item createItem(String name, String code, BigDecimal unitPrice, 
                          BigDecimal discount, int reorderLevel) {
        try {
            ItemCode itemCode = new ItemCode(code);
            
            if (itemRepository.existsByCode(itemCode)) {
                throw new IllegalArgumentException("Item with code " + code + " already exists");
            }
            
            Item item = new Item(
                name,
                itemCode,
                new Money(unitPrice),
                discount,
                reorderLevel
            );
            
            Item savedItem = itemRepository.save(item);
            logger.info("Created new item: " + savedItem.getName());
            return savedItem;
            
        } catch (Exception e) {
            logger.error("Error creating item: " + name, e);
            throw new RuntimeException("Failed to create item", e);
        }
    }
    
    /**
     * Find item by code.
     */
    public Optional<Item> findItemByCode(String code) {
        try {
            ItemCode itemCode = new ItemCode(code);
            return itemRepository.findByCode(itemCode);
        } catch (Exception e) {
            logger.error("Error finding item by code: " + code, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get all items.
     */
    public List<Item> getAllItems() {
        try {
            return itemRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all items", e);
            throw new RuntimeException("Failed to retrieve items", e);
        }
    }
    
    /**
     * Update item details.
     */
    public void updateItem(Item item) {
        try {
            itemRepository.update(item);
            logger.info("Updated item: " + item.getName());
        } catch (Exception e) {
            logger.error("Error updating item: " + item.getName(), e);
            throw new RuntimeException("Failed to update item", e);
        }
    }
    
    /**
     * Delete item.
     */
    public void deleteItem(Long itemId) {
        try {
            itemRepository.delete(itemId);
            logger.info("Deleted item with ID: " + itemId);
        } catch (Exception e) {
            logger.error("Error deleting item with ID: " + itemId, e);
            throw new RuntimeException("Failed to delete item", e);
        }
    }
}