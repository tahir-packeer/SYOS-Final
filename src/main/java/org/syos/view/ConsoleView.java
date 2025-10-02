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
            if (scanner.hasNextLine()) {
                return Integer.parseInt(scanner.nextLine().trim());
            } else {
                return -1;
            }
        } catch (NumberFormatException | java.util.NoSuchElementException e) {
            return -1;
        }
    }

    /**
     * Get string input from user.
     */
    protected String getInput(String prompt) {
        System.out.print(prompt);
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            } else {
                return "";
            }
        } catch (Exception e) {
            System.out.println("\n[Input error, using empty string]");
            return "";
        }
    }

    /**
     * Get integer input from user.
     */
    protected int getIntInput(String prompt) {
        int attempts = 0;
        while (attempts < 5) { // Limit attempts to prevent infinite loop
            try {
                System.out.print(prompt);
                if (scanner.hasNextLine()) {
                    return Integer.parseInt(scanner.nextLine().trim());
                } else {
                    System.out.println("No input available. Using default value 0.");
                    return 0;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
                attempts++;
            } catch (Exception e) {
                System.out.println("Input error. Using default value 0.");
                return 0;
            }
        }
        System.out.println("Too many invalid attempts. Using default value 0.");
        return 0;
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
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            } else {
                // If no input available, just wait a moment
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // If scanner fails, just continue
            System.out.println("\n[Continuing...]");
        }
    }

    /**
     * Center text in given width.
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
