// File: src/main/java/org/syos/application/usecase/BillingAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.*;
import org.syos.application.service.*;
import org.syos.domain.entity.*;
import org.syos.domain.enums.PaymentMethod;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;
import org.syos.infrastructure.persistence.TransactionManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Application service for billing operations.
 * Orchestrates the billing workflow using multiple repositories.
 */
public class BillingAppService {
    private final ItemRepository itemRepository;
    private final BillRepository billRepository;
    private final ShelfStockRepository shelfStockRepository;
    private final WebsiteInventoryRepository websiteInventoryRepository;
    private final CustomerRepository customerRepository;
    private final SerialNumberGenerator serialNumberGenerator;
    private final BillPrinter billPrinter;
    private final PaymentGateway paymentGateway;
    private final TransactionManager transactionManager;
    private final Logger logger;

    public BillingAppService(ItemRepository itemRepository,
            BillRepository billRepository,
            ShelfStockRepository shelfStockRepository,
            WebsiteInventoryRepository websiteInventoryRepository,
            CustomerRepository customerRepository,
            SerialNumberGenerator serialNumberGenerator,
            BillPrinter billPrinter,
            PaymentGateway paymentGateway,
            TransactionManager transactionManager,
            Logger logger) {
        this.itemRepository = itemRepository;
        this.billRepository = billRepository;
        this.shelfStockRepository = shelfStockRepository;
        this.websiteInventoryRepository = websiteInventoryRepository;
        this.customerRepository = customerRepository;
        this.serialNumberGenerator = serialNumberGenerator;
        this.billPrinter = billPrinter;
        this.paymentGateway = paymentGateway;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    /**
     * Process a counter sale (in-store transaction).
     */
    public Bill processCounterSale(List<OrderItem> orderItems, String customerName,
            Money cashTendered) {
        return processSale(orderItems, customerName, TransactionType.COUNTER,
                PaymentMethod.CASH, cashTendered, Money.zero());
    }

    /**
     * Process a counter sale with manual discount.
     */
    public Bill processCounterSale(List<OrderItem> orderItems, String customerName,
            Money cashTendered, Money manualDiscount) {
        return processSale(orderItems, customerName, TransactionType.COUNTER,
                PaymentMethod.CASH, cashTendered, manualDiscount);
    }

    /**
     * Process an online sale.
     */
    public Bill processOnlineSale(List<OrderItem> orderItems, String customerName,
            PaymentMethod paymentMethod) {
        return processSale(orderItems, customerName, TransactionType.ONLINE,
                paymentMethod, null, Money.zero());
    }

    /**
     * Main billing workflow.
     * Uses TransactionManager to ensure atomicity.
     */
    private Bill processSale(List<OrderItem> orderItems, String customerName,
            TransactionType transactionType, PaymentMethod paymentMethod,
            Money cashTendered, Money manualDiscount) {
        return transactionManager.executeInTransaction(conn -> {
            try {
                // 1. Validate items and build bill items
                List<BillItem> billItems = new ArrayList<>();

                for (OrderItem orderItem : orderItems) {
                    Optional<Item> itemOpt = itemRepository.findByCode(
                            new ItemCode(orderItem.getItemCode()));

                    if (itemOpt.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Item not found: " + orderItem.getItemCode());
                    }

                    Item item = itemOpt.get();

                    // Check stock availability
                    if (!checkStockAvailability(item.getCode(), orderItem.getQuantity(),
                            transactionType)) {
                        throw new IllegalArgumentException(
                                "Insufficient stock for item: " + item.getName());
                    }

                    BillItem billItem = new BillItem(item, orderItem.getQuantity());
                    billItems.add(billItem);
                }

                // 2. Generate serial number
                long nextSerial = billRepository.getNextSerialNumber();
                String serialNumber = serialNumberGenerator.generateSerialNumber(nextSerial);

                // 3. Build bill
                Bill.Builder billBuilder = new Bill.Builder()
                        .serialNumber(serialNumber)
                        .dateTime(LocalDateTime.now())
                        .transactionType(transactionType)
                        .customerName(customerName)
                        .items(billItems)
                        .paymentMethod(paymentMethod)
                        .discount(manualDiscount != null ? manualDiscount : Money.zero());

                if (cashTendered != null) {
                    billBuilder.cashTendered(cashTendered);
                }

                Bill bill = billBuilder.build();

                // 4. Validate payment
                if (paymentMethod == PaymentMethod.CASH) {
                    if (cashTendered == null || cashTendered.isLessThan(bill.getTotalAmount())) {
                        throw new IllegalArgumentException("Insufficient cash tendered");
                    }
                } else {
                    // Process electronic payment
                    boolean paymentSuccess = paymentGateway.processPayment(
                            bill.getTotalAmount(), paymentMethod, customerName);
                    if (!paymentSuccess) {
                        throw new RuntimeException("Payment processing failed");
                    }
                }

                // 5. Save bill
                Bill savedBill = billRepository.save(bill);

                // 6. Update stock
                updateStockAfterSale(billItems, transactionType);

                // 7. Print bill
                billPrinter.print(savedBill);

                logger.info("Processed " + transactionType + " sale: " + serialNumber);
                return savedBill;

            } catch (Exception e) {
                logger.error("Error processing sale", e);
                throw new RuntimeException("Failed to process sale: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Check if sufficient stock is available.
     */
    private boolean checkStockAvailability(ItemCode itemCode, int quantity,
            TransactionType transactionType) {
        try {
            if (transactionType == TransactionType.COUNTER) {
                Optional<ShelfStock> stockOpt = shelfStockRepository.findByItemCode(itemCode);
                return stockOpt.isPresent() && stockOpt.get().getQuantity() >= quantity;
            } else {
                Optional<WebsiteInventory> invOpt = websiteInventoryRepository.findByItemCode(itemCode);
                return invOpt.isPresent() && invOpt.get().isInStock(quantity);
            }
        } catch (Exception e) {
            logger.error("Error checking stock availability", e);
            return false;
        }
    }

    /**
     * Update stock after successful sale.
     */
    private void updateStockAfterSale(List<BillItem> billItems,
            TransactionType transactionType) {
        for (BillItem billItem : billItems) {
            ItemCode itemCode = billItem.getItem().getCode();
            int quantity = billItem.getQuantity();

            if (transactionType == TransactionType.COUNTER) {
                Optional<ShelfStock> stockOpt = shelfStockRepository.findByItemCode(itemCode);
                if (stockOpt.isPresent()) {
                    ShelfStock stock = stockOpt.get();
                    stock.reduceStock(quantity);
                    shelfStockRepository.update(stock);
                }
            } else {
                Optional<WebsiteInventory> invOpt = websiteInventoryRepository.findByItemCode(itemCode);
                if (invOpt.isPresent()) {
                    WebsiteInventory inventory = invOpt.get();
                    inventory.reduceStock(quantity);
                    websiteInventoryRepository.update(inventory);
                }
            }
        }
    }

    /**
     * Find or register customer by phone number.
     */
    public Optional<Customer> findOrRegisterCustomer(String customerName, String phoneNumber) {
        try {
            // Try to find existing customer by phone
            Optional<Customer> existingCustomer = customerRepository.findByPhone(phoneNumber);

            if (existingCustomer.isPresent()) {
                logger.info("Found existing customer: " + existingCustomer.get().getName());
                return existingCustomer;
            }

            // Register new customer
            Customer newCustomer = new Customer(customerName, phoneNumber);
            Customer savedCustomer = customerRepository.save(newCustomer);
            logger.info("Registered new customer: " + customerName + " (" + phoneNumber + ")");
            return Optional.of(savedCustomer);

        } catch (Exception e) {
            logger.error("Error finding/registering customer", e);
            return Optional.empty();
        }
    }

    /**
     * Preview bill without processing payment.
     */
    public BillPreview previewBill(List<OrderItem> orderItems, String customerName, Money manualDiscount) {
        try {
            List<BillItem> billItems = new ArrayList<>();
            Money subtotal = Money.zero();

            for (OrderItem orderItem : orderItems) {
                Optional<Item> itemOpt = itemRepository.findByCode(new ItemCode(orderItem.getItemCode()));

                if (itemOpt.isEmpty()) {
                    throw new IllegalArgumentException("Item not found: " + orderItem.getItemCode());
                }

                Item item = itemOpt.get();
                BillItem billItem = new BillItem(item, orderItem.getQuantity());
                billItems.add(billItem);
                subtotal = subtotal.add(billItem.getTotalPrice());
            }

            Money discount = manualDiscount != null ? manualDiscount : Money.zero();
            Money total = subtotal.subtract(discount);

            return new BillPreview(billItems, subtotal, discount, total, customerName);

        } catch (Exception e) {
            logger.error("Error generating bill preview", e);
            throw new RuntimeException("Failed to generate bill preview: " + e.getMessage(), e);
        }
    }

    /**
     * Helper class for order items.
     */
    public static class OrderItem {
        private final String itemCode;
        private final int quantity;

        public OrderItem(String itemCode, int quantity) {
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
     * Bill preview class for showing totals before payment.
     */
    public static class BillPreview {
        private final List<BillItem> items;
        private final Money subtotal;
        private final Money discount;
        private final Money total;
        private final String customerName;

        public BillPreview(List<BillItem> items, Money subtotal, Money discount, Money total, String customerName) {
            this.items = items;
            this.subtotal = subtotal;
            this.discount = discount;
            this.total = total;
            this.customerName = customerName;
        }

        // Getters
        public List<BillItem> getItems() {
            return items;
        }

        public Money getSubtotal() {
            return subtotal;
        }

        public Money getDiscount() {
            return discount;
        }

        public Money getTotal() {
            return total;
        }

        public String getCustomerName() {
            return customerName;
        }
    }
}