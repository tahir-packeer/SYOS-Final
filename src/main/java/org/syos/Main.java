// File: src/main/java/org/syos/Main.java
package org.syos;

import org.syos.application.repository.*;
import org.syos.application.service.*;
import org.syos.application.usecase.*;
import org.syos.controller.*;
import org.syos.infrastructure.config.ConfigManager;
import org.syos.infrastructure.external.*;
import org.syos.infrastructure.persistence.*;
import org.syos.infrastructure.util.PasswordHasher;
import java.util.Scanner;

/**
 * Main application entry point.
 * Dependency Injection and wiring of components.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Starting SYOS-POS System...");
        
        try {
            // Initialize configuration
            ConfigManager config = ConfigManager.getInstance();
            System.out.println("Configuration loaded");
            
            // Initialize database connection pool
            DBConnection dbConnection = DBConnection.getInstance();
            System.out.println("Database connection pool initialized");
            
            // Initialize infrastructure components
            TransactionManager transactionManager = new TransactionManager(dbConnection);
            PasswordHasher passwordHasher = new PasswordHasher();
            
            // Initialize external services
            Logger logger = new SimpleConsoleLogger();
            BillPrinter billPrinter = new ConsoleBillPrinter();
            SerialNumberGenerator serialNumberGenerator = new UUIDSerialNumberGenerator();
            PaymentGateway paymentGateway = new MockPaymentGateway();
            
            // Initialize repositories
            UserRepository userRepository = new UserRepositoryStub(); // Using stub for CCCP1
            ItemRepository itemRepository = new ItemRepositoryImpl(dbConnection);
            CustomerRepository customerRepository = new CustomerRepositoryStub();
            OnlineCustomerRepository onlineCustomerRepository = new OnlineCustomerRepositoryStub();
            BillRepository billRepository = new BillRepositoryStub();
            StockBatchRepository stockBatchRepository = new StockBatchRepositoryStub();
            ShelfStockRepository shelfStockRepository = new ShelfStockRepositoryStub();
            WebsiteInventoryRepository websiteInventoryRepository = new WebsiteInventoryRepositoryStub();
            
            // Initialize application services (use cases)
            AuthenticationAppService authService = new AuthenticationAppService(
                userRepository, passwordHasher, logger
            );
            
            ItemAppService itemService = new ItemAppService(itemRepository, logger);
            
            BillingAppService billingService = new BillingAppService(
                itemRepository, billRepository, shelfStockRepository, websiteInventoryRepository,
                serialNumberGenerator, billPrinter, paymentGateway, transactionManager, logger
            );
            
            StockAppService stockService = new StockAppService(
                itemRepository, stockBatchRepository, shelfStockRepository,
                websiteInventoryRepository, transactionManager, logger
            );
            
            ReportAppService reportService = new ReportAppService(
                billRepository, stockBatchRepository, shelfStockRepository, logger
            );
            
            CustomerAppService customerService = new CustomerAppService(
                customerRepository, logger
            );
            
            OnlineCustomerAppService onlineCustomerService = new OnlineCustomerAppService(
                onlineCustomerRepository, passwordHasher, logger
            );
            
            // Initialize controllers
            CashierController cashierController = new CashierController(
                billingService, itemService, reportService
            );
            
            ManagerController managerController = new ManagerController(
                itemService, stockService, reportService, customerService
            );
            
            // Initialize main controller and scanner
            Scanner scanner = new Scanner(System.in);
            MainController mainController = new MainController(
                scanner, authService, cashierController, managerController
            );
            
            // Add shutdown hook to close connections gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down SYOS-POS System...");
                dbConnection.closeAllConnections();
                scanner.close();
                System.out.println("Shutdown complete");
            }));
            
            // Run application
            mainController.run();
            
        } catch (Exception e) {
            System.err.println("Fatal error starting application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // ========================================================================
    // STUB IMPLEMENTATIONS FOR CCCP1 (Replace with real implementations)
    // ========================================================================
    
    /**
     * Stub implementation of UserRepository for demonstration.
     * In production, implement full JDBC version like ItemRepositoryImpl.
     */
    static class UserRepositoryStub implements UserRepository {
        private final java.util.Map<String, org.syos.domain.entity.User> users = new java.util.HashMap<>();
        
        public UserRepositoryStub() {
            // Create default users for testing
            PasswordHasher hasher = new PasswordHasher();
            
            users.put("cashier", new org.syos.domain.entity.User(
                1L, "cashier", 
                new org.syos.domain.valueobject.Password(hasher.hash("cashier123")),
                "John Cashier", org.syos.domain.enums.UserRole.CASHIER, true
            ));
            
            users.put("manager", new org.syos.domain.entity.User(
                2L, "manager",
                new org.syos.domain.valueobject.Password(hasher.hash("manager123")),
                "Jane Manager", org.syos.domain.enums.UserRole.MANAGER, true
            ));
            
            users.put("admin", new org.syos.domain.entity.User(
                3L, "admin",
                new org.syos.domain.valueobject.Password(hasher.hash("admin123")),
                "Admin User", org.syos.domain.enums.UserRole.ADMIN, true
            ));
        }
        
        @Override
        public org.syos.domain.entity.User save(org.syos.domain.entity.User user) {
            users.put(user.getUsername(), user);
            return user;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.User> findById(Long userId) {
            return users.values().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.User> findByUsername(String username) {
            return java.util.Optional.ofNullable(users.get(username));
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.User> findAll() {
            return new java.util.ArrayList<>(users.values());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.User> findByRole(org.syos.domain.enums.UserRole role) {
            return users.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public void update(org.syos.domain.entity.User user) {
            users.put(user.getUsername(), user);
        }
        
        @Override
        public void delete(Long userId) {
            users.entrySet().removeIf(e -> e.getValue().getUserId().equals(userId));
        }
        
        @Override
        public boolean existsByUsername(String username) {
            return users.containsKey(username);
        }
    }
    
    static class CustomerRepositoryStub implements CustomerRepository {
        private final java.util.List<org.syos.domain.entity.Customer> customers = new java.util.ArrayList<>();
        private long nextId = 1;
        
        @Override
        public org.syos.domain.entity.Customer save(org.syos.domain.entity.Customer customer) {
            customer.setCustomerId(nextId++);
            customers.add(customer);
            return customer;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.Customer> findById(Long customerId) {
            return customers.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .findFirst();
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.Customer> findByPhone(String phone) {
            return customers.stream()
                .filter(c -> c.getPhone().equals(phone))
                .findFirst();
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Customer> findAll() {
            return new java.util.ArrayList<>(customers);
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Customer> searchByName(String name) {
            return customers.stream()
                .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public void update(org.syos.domain.entity.Customer customer) {
            // Update in place
        }
        
        @Override
        public void delete(Long customerId) {
            customers.removeIf(c -> c.getCustomerId().equals(customerId));
        }
    }
    
    static class OnlineCustomerRepositoryStub implements OnlineCustomerRepository {
        private final java.util.List<org.syos.domain.entity.OnlineCustomer> customers = new java.util.ArrayList<>();
        private long nextId = 1;
        
        @Override
        public org.syos.domain.entity.OnlineCustomer save(org.syos.domain.entity.OnlineCustomer customer) {
            customer.setOnlineCustomerId(nextId++);
            customers.add(customer);
            return customer;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.OnlineCustomer> findById(Long customerId) {
            return customers.stream()
                .filter(c -> c.getOnlineCustomerId().equals(customerId))
                .findFirst();
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.OnlineCustomer> findByEmail(String email) {
            return customers.stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst();
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.OnlineCustomer> findAll() {
            return new java.util.ArrayList<>(customers);
        }
        
        @Override
        public void update(org.syos.domain.entity.OnlineCustomer customer) {
            // Update in place
        }
        
        @Override
        public void delete(Long customerId) {
            customers.removeIf(c -> c.getOnlineCustomerId().equals(customerId));
        }
        
        @Override
        public boolean existsByEmail(String email) {
            return customers.stream().anyMatch(c -> c.getEmail().equals(email));
        }
    }
    
    static class BillRepositoryStub implements BillRepository {
        private final java.util.List<org.syos.domain.entity.Bill> bills = new java.util.ArrayList<>();
        private long nextId = 1;
        private long nextSerial = 1;
        
        @Override
        public org.syos.domain.entity.Bill save(org.syos.domain.entity.Bill bill) {
            bill.setBillId(nextId++);
            bills.add(bill);
            return bill;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.Bill> findById(Long billId) {
            return bills.stream()
                .filter(b -> b.getBillId().equals(billId))
                .findFirst();
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.Bill> findBySerialNumber(String serialNumber) {
            return bills.stream()
                .filter(b -> b.getSerialNumber().equals(serialNumber))
                .findFirst();
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Bill> findAll() {
            return new java.util.ArrayList<>(bills);
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Bill> findByDate(java.time.LocalDate date) {
            return bills.stream()
                .filter(b -> b.getDateTime().toLocalDate().equals(date))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Bill> findByTransactionType(org.syos.domain.enums.TransactionType type) {
            return bills.stream()
                .filter(b -> b.getTransactionType() == type)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.Bill> findByDateAndType(java.time.LocalDate date, 
                                                                              org.syos.domain.enums.TransactionType type) {
            return bills.stream()
                .filter(b -> b.getDateTime().toLocalDate().equals(date) && b.getTransactionType() == type)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public long getNextSerialNumber() {
            return nextSerial++;
        }
    }
    
    static class StockBatchRepositoryStub implements StockBatchRepository {
        private final java.util.List<org.syos.domain.entity.StockBatch> batches = new java.util.ArrayList<>();
        private long nextId = 1;
        
        @Override
        public org.syos.domain.entity.StockBatch save(org.syos.domain.entity.StockBatch batch) {
            batch.setBatchId(nextId++);
            batches.add(batch);
            return batch;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.StockBatch> findById(Long batchId) {
            return batches.stream()
                .filter(b -> b.getBatchId().equals(batchId))
                .findFirst();
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.StockBatch> findByItemCode(org.syos.domain.valueobject.ItemCode code) {
            return batches.stream()
                .filter(b -> b.getItem().getCode().equals(code))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.StockBatch> findByItemCodeOrderedByDate(
                org.syos.domain.valueobject.ItemCode code) {
            return batches.stream()
                .filter(b -> b.getItem().getCode().equals(code))
                .sorted(java.util.Comparator.comparing(org.syos.domain.entity.StockBatch::getPurchaseDate))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.StockBatch> findAll() {
            return new java.util.ArrayList<>(batches);
        }
        
        @Override
        public void update(org.syos.domain.entity.StockBatch batch) {
            // Update in place
        }
        
        @Override
        public void delete(Long batchId) {
            batches.removeIf(b -> b.getBatchId().equals(batchId));
        }
    }
    
    static class ShelfStockRepositoryStub implements ShelfStockRepository {
        private final java.util.Map<org.syos.domain.valueobject.ItemCode, org.syos.domain.entity.ShelfStock> stock = 
            new java.util.HashMap<>();
        private long nextId = 1;
        
        @Override
        public org.syos.domain.entity.ShelfStock save(org.syos.domain.entity.ShelfStock shelfStock) {
            shelfStock.setShelfStockId(nextId++);
            stock.put(shelfStock.getItem().getCode(), shelfStock);
            return shelfStock;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.ShelfStock> findByItemCode(org.syos.domain.valueobject.ItemCode code) {
            return java.util.Optional.ofNullable(stock.get(code));
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.ShelfStock> findAll() {
            return new java.util.ArrayList<>(stock.values());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.ShelfStock> findBelowReorderLevel() {
            return stock.values().stream()
                .filter(org.syos.domain.entity.ShelfStock::isBelowReorderLevel)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public void update(org.syos.domain.entity.ShelfStock shelfStock) {
            stock.put(shelfStock.getItem().getCode(), shelfStock);
        }
        
        @Override
        public void delete(org.syos.domain.valueobject.ItemCode code) {
            stock.remove(code);
        }
    }
    
    static class WebsiteInventoryRepositoryStub implements WebsiteInventoryRepository {
        private final java.util.Map<org.syos.domain.valueobject.ItemCode, org.syos.domain.entity.WebsiteInventory> inventory = 
            new java.util.HashMap<>();
        private long nextId = 1;
        
        @Override
        public org.syos.domain.entity.WebsiteInventory save(org.syos.domain.entity.WebsiteInventory websiteInventory) {
            websiteInventory.setWebInventoryId(nextId++);
            inventory.put(websiteInventory.getItem().getCode(), websiteInventory);
            return websiteInventory;
        }
        
        @Override
        public java.util.Optional<org.syos.domain.entity.WebsiteInventory> findByItemCode(
                org.syos.domain.valueobject.ItemCode code) {
            return java.util.Optional.ofNullable(inventory.get(code));
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.WebsiteInventory> findAll() {
            return new java.util.ArrayList<>(inventory.values());
        }
        
        @Override
        public java.util.List<org.syos.domain.entity.WebsiteInventory> findAvailableItems() {
            return inventory.values().stream()
                .filter(inv -> inv.getQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public void update(org.syos.domain.entity.WebsiteInventory websiteInventory) {
            inventory.put(websiteInventory.getItem().getCode(), websiteInventory);
        }
        
        @Override
        public void delete(org.syos.domain.valueobject.ItemCode code) {
            inventory.remove(code);
        }
    }
}