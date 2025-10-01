// File: src/main/java/org/syos/view/ConsoleView.java
package org.syos.view;

import java.util.Scanner;

/**
 * Base class for console views.
 * Provides common UI utilities.
 */
public abstract class ConsoleView {
    protected final Scanner scanner;
    
    public ConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }
    
    /**
     * Display the view and handle user interaction.
     */
    public abstract void display();
    
    /**
     * Clear console (works on most terminals).
     */
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Display a header.
     */
    protected void displayHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(centerText(title, 50));
        System.out.println("=".repeat(50));
    }
    
    /**
     * Display a menu and get user choice.
     */
    protected int displayMenu(String title, String[] options) {
        displayHeader(title);
        System.out.println();
        
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        
        System.out.println("0. Back/Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
        
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Get string input from user.
     */
    protected String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Get integer input from user.
     */
    protected int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
    
    /**
     * Display error message.
     */
    protected void displayError(String message) {
        System.out.println("\n*** ERROR: " + message + " ***\n");
    }
    
    /**
     * Display success message.
     */
    protected void displaySuccess(String message) {
        System.out.println("\n*** SUCCESS: " + message + " ***\n");
    }
    
    /**
     * Pause and wait for user to press Enter.
     */
    protected void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Center text in given width.
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}