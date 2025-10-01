// File: src/main/java/org/syos/Main.java
package org.syos;

import org.syos.application.repository.*;
import org.syos.application.service.*;
import org.syos.application.usecase.*;
import org.syos.controller.*;
import org.syos.infrastructure.config.ConfigManager;
import org.syos.infrastructure.external.*;
import org.syos.infrastructure.persistence.*;

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

            // Initialize external services
            Logger logger = new SimpleConsoleLogger();
            BillPrinter billPrinter = new EnhancedBillPrinter();
            SerialNumberGenerator serialNumberGenerator = new UUIDSerialNumberGenerator();
            PaymentGateway paymentGateway = new MockPaymentGateway();

            // Initialize repositories
            UserRepository userRepository = new UserRepositoryImpl(dbConnection);
            ItemRepository itemRepository = new ItemRepositoryImpl(dbConnection);
            CustomerRepository customerRepository = new CustomerRepositoryImpl(dbConnection);
            OnlineCustomerRepository onlineCustomerRepository = new OnlineCustomerRepositoryImpl(dbConnection);
            BillRepository billRepository = new BillRepositoryImpl(dbConnection);
            StockBatchRepository stockBatchRepository = new StockBatchRepositoryImpl(dbConnection);
            ShelfStockRepository shelfStockRepository = new ShelfStockRepositoryImpl(dbConnection);
            WebsiteInventoryRepository websiteInventoryRepository = new WebsiteInventoryRepositoryImpl(dbConnection);

            // Initialize application services (use cases) - simplified authentication
            AuthenticationAppService authService = new AuthenticationAppService(userRepository, logger);

            ItemAppService itemService = new ItemAppService(itemRepository, logger);

            BillingAppService billingService = new BillingAppService(
                    itemRepository, billRepository, shelfStockRepository, websiteInventoryRepository,
                    customerRepository, serialNumberGenerator, billPrinter, paymentGateway, transactionManager, logger);

            StockAppService stockService = new StockAppService(
                    itemRepository, stockBatchRepository, shelfStockRepository,
                    websiteInventoryRepository, transactionManager, logger);

            ReportAppService reportService = new ReportAppService(
                    billRepository, stockBatchRepository, shelfStockRepository, logger);

            CustomerAppService customerService = new CustomerAppService(
                    customerRepository, logger);

            OnlineCustomerAppService onlineCustomerService = new OnlineCustomerAppService(
                    onlineCustomerRepository, logger);

            // Initialize controllers
            CashierController cashierController = new CashierController(
                    billingService, itemService, reportService);

            ManagerController managerController = new ManagerController(
                    itemService, stockService, reportService, customerService);

            // Initialize main controller and scanner
            Scanner scanner = new Scanner(System.in);
            MainController mainController = new MainController(
                    scanner, authService, cashierController, managerController);

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

}