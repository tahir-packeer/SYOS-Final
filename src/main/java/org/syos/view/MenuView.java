// File: src/main/java/org/syos/view/MenuView.java
package org.syos.view;

import java.util.Scanner;

/**
 * Main menu view for login and role selection.
 */
public class MenuView extends ConsoleView {
    
    public MenuView(Scanner scanner) {
        super(scanner);
    }
    
    @Override
    public void display() {
        displayHeader("SYNEX OUTLET STORE - POS SYSTEM");
        System.out.println("\nWelcome to SYOS-POS System");
        System.out.println("Version 1.0 - CCCP1");
    }
    
    /**
     * Get login credentials from user.
     */
    public String[] getLoginCredentials() {
        displayHeader("LOGIN");
        String username = getInput("Username: ");
        String password = getInput("Password: ");
        return new String[]{username, password};
    }
    
    /**
     * Display login success message.
     */
    public void displayLoginSuccess(String username, String role) {
        displaySuccess("Login successful! Welcome " + username + " (" + role + ")");
        pause();
    }
    
    /**
     * Display login failure message.
     */
    public void displayLoginFailure() {
        displayError("Invalid username or password");
        pause();
    }
}