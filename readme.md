/*
 * ============================================================================
 * SYOS-POS SYSTEM - COMPLETE PROJECT STRUCTURE
 * ============================================================================
 * 
 * PROJECT LAYOUT:
 * 
 * src/main/java/org/syos/
 * ├── domain/
 * │   ├── entity/
 * │   │   ├── Item.java
 * │   │   ├── Customer.java
 * │   │   ├── OnlineCustomer.java
 * │   │   ├── User.java
 * │   │   ├── Bill.java
 * │   │   ├── BillItem.java
 * │   │   ├── StockBatch.java
 * │   │   ├── ShelfStock.java
 * │   │   └── WebsiteInventory.java
 * │   ├── valueobject/
 * │   │   ├── Money.java
 * │   │   ├── Quantity.java
 * │   │   ├── Password.java
 * │   │   └── ItemCode.java
 * │   └── enums/
 * │       ├── UserRole.java
 * │       ├── TransactionType.java
 * │       └── PaymentMethod.java
 * ├── application/
 * │   ├── repository/
 * │   │   ├── UserRepository.java
 * │   │   ├── ItemRepository.java
 * │   │   ├── CustomerRepository.java
 * │   │   ├── OnlineCustomerRepository.java
 * │   │   ├── BillRepository.java
 * │   │   ├── StockBatchRepository.java
 * │   │   ├── ShelfStockRepository.java
 * │   │   └── WebsiteInventoryRepository.java
 * │   ├── service/
 * │   │   ├── BillPrinter.java
 * │   │   ├── Logger.java
 * │   │   ├── SerialNumberGenerator.java
 * │   │   └── PaymentGateway.java
 * │   ├── dto/
 * │   │   ├── ItemDTO.java
 * │   │   ├── BillDTO.java
 * │   │   └── ReportDTO.java
 * │   └── usecase/
 * │       ├── AuthenticationAppService.java
 * │       ├── BillingAppService.java
 * │       ├── ItemAppService.java
 * │       ├── StockAppService.java
 * │       ├── ReportAppService.java
 * │       ├── CustomerAppService.java
 * │       ├── OnlineCustomerAppService.java
 * │       └── WebsiteSalesAppService.java
 * ├── infrastructure/
 * │   ├── persistence/
 * │   │   ├── DBConnection.java
 * │   │   ├── TransactionManager.java
 * │   │   ├── UserRepositoryImpl.java
 * │   │   ├── ItemRepositoryImpl.java
 * │   │   ├── CustomerRepositoryImpl.java
 * │   │   ├── OnlineCustomerRepositoryImpl.java
 * │   │   ├── BillRepositoryImpl.java
 * │   │   ├── StockBatchRepositoryImpl.java
 * │   │   ├── ShelfStockRepositoryImpl.java
 * │   │   └── WebsiteInventoryRepositoryImpl.java
 * │   ├── config/
 * │   │   └── ConfigManager.java
 * │   ├── util/
 * │   │   ├── PasswordHasher.java
 * │   │   └── ValidationUtil.java
 * │   └── external/
 * │       ├── ConsoleBillPrinter.java
 * │       ├── SimpleConsoleLogger.java
 * │       ├── UUIDSerialNumberGenerator.java
 * │       └── MockPaymentGateway.java
 * ├── view/
 * │   ├── ConsoleView.java
 * │   ├── CashierView.java
 * │   ├── ManagerView.java
 * │   └── MenuView.java
 * ├── controller/
 * │   ├── MainController.java
 * │   ├── CashierController.java
 * │   ├── ManagerController.java
 * │   └── command/
 * │       ├── Command.java
 * │       ├── BillingCommand.java
 * │       └── ReportCommand.java
 * └── Main.java
 * 
 * src/main/resources/
 * ├── application.properties
 * ├── schema.sql
 * └── logback.xml
 * 
 * src/test/java/org/syos/
 * ├── domain/
 * ├── application/
 * └── infrastructure/
 * 
 * pom.xml
 * 
 * ============================================================================
 * 
 * This structure follows:
 * - MVC Architecture
 * - Clean Architecture (Domain → Application → Infrastructure)
 * - SOLID Principles
 * - Design Patterns: Singleton, Repository, Facade, Strategy, Builder, etc.
 * 
 * ============================================================================
 */



