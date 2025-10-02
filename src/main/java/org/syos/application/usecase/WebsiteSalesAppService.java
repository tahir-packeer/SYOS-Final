package org.syos.application.usecase;

import org.syos.application.repository.*;
import org.syos.application.service.Logger;
import org.syos.domain.entity.*;
import org.syos.domain.enums.PaymentMethod;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.ItemCode;
import org.syos.infrastructure.persistence.TransactionManager;
import java.util.*;

/**
 * Application service for website sales operations.
 * Handles online customer registration, authentication, and order processing.
 */
public class WebsiteSalesAppService {
    private final OnlineCustomerRepository onlineCustomerRepository;
    private final ItemRepository itemRepository;
    private final WebsiteInventoryRepository websiteInventoryRepository;
    private final BillRepository billRepository;
    private final TransactionManager transactionManager;
    private final Logger logger;

    public WebsiteSalesAppService(OnlineCustomerRepository onlineCustomerRepository,
            ItemRepository itemRepository,
            WebsiteInventoryRepository websiteInventoryRepository,
            BillRepository billRepository,
            TransactionManager transactionManager,
            Logger logger) {
        this.onlineCustomerRepository = onlineCustomerRepository;
        this.itemRepository = itemRepository;
        this.websiteInventoryRepository = websiteInventoryRepository;
        this.billRepository = billRepository;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    /**
     * Register a new online customer.
     */
    public OnlineCustomer registerOnlineCustomer(String name, String email,
            String address, String password) {
        return transactionManager.executeInTransaction(conn -> {
            try {
                // Check if customer already exists
                Optional<OnlineCustomer> existingCustomer = onlineCustomerRepository.findByEmail(email);
                if (existingCustomer.isPresent()) {
                    throw new IllegalArgumentException("Customer already exists with email: " + email);
                }

                org.syos.domain.valueobject.Password hashedPassword = new org.syos.domain.valueobject.Password(
                        password);
                OnlineCustomer customer = new OnlineCustomer(name, email, address, hashedPassword);

                OnlineCustomer savedCustomer = onlineCustomerRepository.save(customer);
                logger.info("Registered new online customer: " + email);
                return savedCustomer;

            } catch (Exception e) {
                logger.error("Error registering online customer", e);
                throw new RuntimeException("Failed to register online customer", e);
            }
        });
    }

    /**
     * Authenticate an online customer.
     */
    public Optional<OnlineCustomer> authenticateCustomer(String email, String password) {
        try {
            Optional<OnlineCustomer> customerOpt = onlineCustomerRepository.findByEmail(email);

            if (customerOpt.isPresent()) {
                OnlineCustomer customer = customerOpt.get();
                if (customer.getPassword().matches(password)) {
                    logger.info("Online customer authenticated: " + email);
                    return Optional.of(customer);
                }
            }

            logger.info("Failed authentication attempt for online customer: " + email);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error authenticating online customer", e);
            return Optional.empty();
        }
    }

    /**
     * Get all available items for online purchase.
     */
    public List<WebsiteInventory> getAvailableItemsForOnlineSale() {
        try {
            return websiteInventoryRepository.findAvailableItems();
        } catch (Exception e) {
            logger.error("Error getting available items for online sale", e);
            throw new RuntimeException("Failed to get available items", e);
        }
    }

    /**
     * Order item for online sale.
     */
    public static class OnlineOrderItem {
        private final String itemCode;
        private final int quantity;

        public OnlineOrderItem(String itemCode, int quantity) {
            this.itemCode = itemCode;
            this.quantity = quantity;
        }

        public String getItemCode() {
            return itemCode;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * Process online sale.
     */
    public Bill processOnlineSale(List<OnlineOrderItem> orderItems,
            String customerEmail, PaymentMethod paymentMethod) {
        return transactionManager.executeInTransaction(conn -> {
            try {
                // Get customer
                Optional<OnlineCustomer> customerOpt = onlineCustomerRepository.findByEmail(customerEmail);
                if (customerOpt.isEmpty()) {
                    throw new IllegalArgumentException("Online customer not found: " + customerEmail);
                }
                OnlineCustomer customer = customerOpt.get();

                // Prepare bill items
                List<BillItem> billItems = new ArrayList<>();

                // Process each order item
                for (OnlineOrderItem orderItem : orderItems) {
                    ItemCode itemCode = new ItemCode(orderItem.getItemCode());

                    // Get item
                    Optional<Item> itemOpt = itemRepository.findByCode(itemCode);
                    if (itemOpt.isEmpty()) {
                        throw new IllegalArgumentException("Item not found: " + orderItem.getItemCode());
                    }
                    Item item = itemOpt.get();

                    // Check website inventory
                    Optional<WebsiteInventory> inventoryOpt = websiteInventoryRepository.findByItemCode(itemCode);
                    if (inventoryOpt.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Item not available for online sale: " + orderItem.getItemCode());
                    }
                    WebsiteInventory inventory = inventoryOpt.get();

                    if (!inventory.isInStock(orderItem.getQuantity())) {
                        throw new IllegalArgumentException("Insufficient stock for item: " + orderItem.getItemCode() +
                                ". Available: " + inventory.getQuantity() + ", Requested: " + orderItem.getQuantity());
                    }

                    // Reduce inventory
                    inventory.reduceStock(orderItem.getQuantity());
                    websiteInventoryRepository.update(inventory);

                    // Create bill item (discount automatically calculated by
                    // Item.calculateTotalPrice)
                    BillItem billItem = new BillItem(item, orderItem.getQuantity());
                    billItems.add(billItem);
                }

                // Create bill using builder pattern
                Bill bill = new Bill.Builder()
                        .serialNumber("WEB-" + System.currentTimeMillis())
                        .transactionType(TransactionType.ONLINE)
                        .customerId(customer.getOnlineCustomerId())
                        .customerName(customer.getName())
                        .items(billItems)
                        .paymentMethod(paymentMethod)
                        .build();

                // Save bill (bill items are already included in the bill)
                Bill savedBill = billRepository.save(bill);

                logger.info("Processed online sale - Bill ID: " + savedBill.getBillId() +
                        ", Customer: " + customerEmail + ", Amount: " + savedBill.getTotalAmount().toDisplayString());

                return savedBill;

            } catch (Exception e) {
                logger.error("Error processing online sale", e);
                throw new RuntimeException("Failed to process online sale", e);
            }
        });
    }

    /**
     * Get all online customers.
     */
    public List<OnlineCustomer> getAllOnlineCustomers() {
        try {
            return onlineCustomerRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all online customers", e);
            throw new RuntimeException("Failed to get online customers", e);
        }
    }
}
