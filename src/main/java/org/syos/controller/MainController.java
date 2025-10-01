// File: src/main/java/org/syos/controller/MainController.java
package org.syos.controller;

import org.syos.application.usecase.AuthenticationAppService;
import org.syos.domain.entity.User;
import org.syos.domain.enums.UserRole;
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
        MenuView menuView = new MenuView(scanner);
        menuView.display();
        
        boolean running = true;
        
        while (running) {
            // Login
            String[] credentials = menuView.getLoginCredentials();
            String username = credentials[0];
            String password = credentials[1];
            
            Optional<User> userOpt = authService.login(username, password);
            
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                menuView.displayLoginSuccess(currentUser.getFullName(), 
                    currentUser.getRole().getDisplayName());
                
                // Route to appropriate view based on role
                routeToRoleView();
                
            } else {
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
    }
    
    /**
     * Route user to appropriate view based on role.
     */
    private void routeToRoleView() {
        if (currentUser == null) {
            return;
        }
        
        UserRole role = currentUser.getRole();
        
        switch (role) {
            case CASHIER:
                CashierView cashierView = new CashierView(scanner, cashierController);
                cashierView.display();
                break;
            case MANAGER:
            case ADMIN:
                ManagerView managerView = new ManagerView(scanner, managerController);
                managerView.display();
                break;
            default:
                System.out.println("Invalid user role");
                break;
        }
        
        currentUser = null; // Logout
    }
}