package org.syos;

import org.syos.application.repository.*;
import org.syos.application.service.*;
import org.syos.application.usecase.*;
import org.syos.controller.*;
import org.syos.infrastructure.external.*;
import org.syos.infrastructure.persistence.*;
import org.syos.infrastructure.util.LoggingService;

import java.util.Scanner;

/**
 * Main application entry point.
 * Dependency Injection and wiring of components.
 */
public class Main {

        public static void main(String[] args) {
                // Initialize logging and log application startup
                LoggingService.logApplicationStartup();

                try {
                        LoggingService.PerformanceMonitor startupMonitor = LoggingService
                                        .startPerformanceMonitoring("Application Startup");

                        // Initialize database connection
                        DBConnection dbConnection = DBConnection.getInstance();
                        LoggingService.logDatabaseOperation("INITIALIZE", "ConnectionPool", "MySQL database connected");

                        // Initialize infrastructure components
                        TransactionManager transactionManager = new TransactionManager(dbConnection);

                        // Initialize external services
                        Logger logger = new SimpleConsoleLogger();
                        BillPrinter billPrinter = new EnhancedBillPrinter(); // Console + File printing
                        SerialNumberGenerator serialNumberGenerator = new UUIDSerialNumberGenerator();
                        PaymentGateway paymentGateway = new MockPaymentGateway();

                        // Initialize repositories
                        UserRepository userRepository = new UserRepositoryImpl(dbConnection);
                        ItemRepository itemRepository = new ItemRepositoryImpl(dbConnection);
                        CustomerRepository customerRepository = new CustomerRepositoryImpl(dbConnection);
                        OnlineCustomerRepository onlineCustomerRepository = new OnlineCustomerRepositoryImpl(
                                        dbConnection);
                        BillRepository billRepository = new BillRepositoryImpl(dbConnection);
                        StockBatchRepository stockBatchRepository = new StockBatchRepositoryImpl(dbConnection);
                        ShelfStockRepository shelfStockRepository = new ShelfStockRepositoryImpl(dbConnection);
                        WebsiteInventoryRepository websiteInventoryRepository = new WebsiteInventoryRepositoryImpl(
                                        dbConnection);

                        // Initialize application services (use cases) - simplified authentication
                        AuthenticationAppService authService = new AuthenticationAppService(userRepository, logger);

                        ItemAppService itemService = new ItemAppService(itemRepository, logger);

                        BillingAppService billingService = new BillingAppService(
                                        itemRepository, billRepository, shelfStockRepository,
                                        websiteInventoryRepository,
                                        customerRepository, serialNumberGenerator, billPrinter, paymentGateway,
                                        transactionManager, logger);

                        StockAppService stockService = new StockAppService(
                                        itemRepository, stockBatchRepository, shelfStockRepository,
                                        websiteInventoryRepository, transactionManager, logger);

                        ReportAppService reportService = new ReportAppService(
                                        billRepository, stockBatchRepository, shelfStockRepository, logger);

                        CustomerAppService customerService = new CustomerAppService(
                                        customerRepository, logger);

                        // Online customer service for future use
                        new OnlineCustomerAppService(onlineCustomerRepository, logger);

                        // Initialize controllers
                        CashierController cashierController = new CashierController(
                                        billingService, itemService, reportService);

                        ManagerController managerController = new ManagerController(
                                        itemService, stockService, reportService, customerService);

                        // Initialize main controller and scanner
                        Scanner scanner = new Scanner(System.in);
                        MainController mainController = new MainController(
                                        scanner, authService, cashierController, managerController);

                        // Add shutdown hook for cleanup
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                LoggingService.logApplicationShutdown();
                                scanner.close();
                                LoggingService.clearContext();
                        }));

                        startupMonitor.complete();

                        // Run application
                        mainController.run();

                        // Explicitly shutdown after main controller finishes
                        LoggingService.logApplicationShutdown();
                        LoggingService.clearContext();

                        System.out.println("Application shutdown complete. Exiting...");
                        System.exit(0); // Force clean exit

                } catch (Exception e) {
                        LoggingService.logError("Fatal error starting application", e);
                        System.exit(1);
                }
        }

}
