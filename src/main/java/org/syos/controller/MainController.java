// File: src/main/java/org/syos/controller/MainController.java
package org.syos.controller;

import org.syos.application.usecase.AuthenticationAppService;
import org.syos.domain.entity.User;
import org.syos.domain.enums.UserRole;
import org.syos.infrastructure.util.LoggingService;
import org.syos.view.*;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main controller - handles application flow and user authentication.
 */
public class MainController {
    private final Scanner scanner;
    private final AuthenticationAppService authService;
    private final CashierController cashierController;
    private final ManagerController managerController;
    private User currentUser;

    public MainController(Scanner scanner,
            AuthenticationAppService authService,
            CashierController cashierController,
            ManagerController managerController) {
        this.scanner = scanner;
        this.authService = authService;
        this.cashierController = cashierController;
        this.managerController = managerController;
    }

    /**
     * Main application loop.
     */
    public void run() {
        LoggingService.logControllerEntry("MainController", "run");
        LoggingService.PerformanceMonitor runMonitor = LoggingService
                .startPerformanceMonitoring("Main Application Loop");

        try {
            MenuView menuView = new MenuView(scanner);
            menuView.display();

            boolean running = true;

            while (running) {
                // Login
                String[] credentials = menuView.getLoginCredentials();
                String username = credentials[0];
                String password = credentials[1];

                LoggingService.setOperation("USER_LOGIN");
                Optional<User> userOpt = authService.login(username, password);

                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                    LoggingService.logUserLogin(username, true);
                    LoggingService.initializeSession(currentUser.getUserId().toString(),
                            LoggingService.generateCorrelationId());

                    menuView.displayLoginSuccess(currentUser.getFullName(),
                            currentUser.getRole().getDisplayName());

                    // Route to appropriate view based on role
                    routeToRoleView();

                    LoggingService.logUserLogout(username);

                } else {
                    LoggingService.logUserLogin(username, false);
                    menuView.displayLoginFailure();
                }

                // Ask to continue or exit
                System.out.print("\nDo you want to login again? (Y/N): ");
                String choice = scanner.nextLine().trim();
                if (!choice.equalsIgnoreCase("Y")) {
                    running = false;
                }
            }

            System.out.println("\nThank you for using SYOS-POS System!");
            runMonitor.complete();

        } catch (Exception e) {
            runMonitor.completeWithError(e);
            LoggingService.logError("Error in main application loop", e);
            throw e;
        } finally {
            LoggingService.logControllerExit("MainController", "run");
            LoggingService.clearContext();
        }
    }

    /**
     * Route user to appropriate view based on role.
     */
    private void routeToRoleView() {
        LoggingService.logControllerEntry("MainController", "routeToRoleView",
                currentUser != null ? currentUser.getRole().toString() : "null");

        if (currentUser == null) {
            LoggingService.logValidationError("MainController", "currentUser", "null", "User not logged in");
            return;
        }

        UserRole role = currentUser.getRole();
        LoggingService.setOperation("ROLE_ROUTING_" + role.name());

        try {
            switch (role) {
                case CASHIER:
                    LoggingService.setOperation("CASHIER_SESSION");
                    EnhancedCashierView cashierView = new EnhancedCashierView(scanner, cashierController);
                    cashierView.display();
                    break;
                case MANAGER:
                case ADMIN:
                    LoggingService.setOperation("MANAGER_SESSION");
                    ManagerView managerView = new ManagerView(scanner, managerController);
                    managerView.display();
                    break;
                default:
                    LoggingService.logValidationError("MainController", "role", role.toString(), "Invalid user role");
                    System.out.println("Invalid user role");
                    break;
            }
        } catch (Exception e) {
            LoggingService.logError("Error in role routing for role: " + role, e);
            throw e;
        } finally {
            currentUser = null; // Logout
            LoggingService.logControllerExit("MainController", "routeToRoleView");
        }
    }
}
