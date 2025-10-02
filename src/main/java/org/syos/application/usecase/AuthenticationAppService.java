// File: src/main/java/org/syos/application/usecase/AuthenticationAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.UserRepository;
import org.syos.application.service.Logger;
import org.syos.domain.entity.User;
import org.syos.domain.valueobject.Password;
import java.util.Optional;

/**
 * Application service for user authentication (simplified version).
 * Facade pattern - simplifies authentication workflow.
 * Follows Single Responsibility Principle.
 */
public class AuthenticationAppService {
    private final UserRepository userRepository;
    private final Logger logger;

    // Constructor injection for Dependency Inversion (simplified)
    public AuthenticationAppService(UserRepository userRepository, Logger logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    /**
     * Authenticate user with username and password (simplified - plain text
     * comparison).
     */
    public Optional<User> login(String username, String plainPassword) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                logger.warn("Login attempt for non-existent user: " + username);
                return Optional.empty();
            }

            User user = userOpt.get();

            if (!user.isActive()) {
                logger.warn("Login attempt for inactive user: " + username);
                return Optional.empty();
            }

            // Simple plain text password comparison
            boolean passwordValid = user.getPassword().matches(plainPassword);

            if (passwordValid) {
                logger.info("Successful login for user: " + username);
                return Optional.of(user);
            } else {
                logger.warn("Failed login attempt for user: " + username);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error during login for user: " + username, e);
            return Optional.empty();
        }
    }

    /**
     * Change user password (simplified - plain text storage).
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            Optional<User> userOpt = login(username, oldPassword);

            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            // Store new password as plain text
            user.setPassword(new Password(newPassword));

            userRepository.update(user);
            logger.info("Password changed for user: " + username);
            return true;

        } catch (Exception e) {
            logger.error("Error changing password for user: " + username, e);
            return false;
        }
    }
}
