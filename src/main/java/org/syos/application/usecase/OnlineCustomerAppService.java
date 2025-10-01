// File: src/main/java/org/syos/application/usecase/OnlineCustomerAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.OnlineCustomerRepository;
import org.syos.application.service.Logger;
import org.syos.domain.entity.OnlineCustomer;
import org.syos.domain.valueobject.Password;
import java.util.Optional;

/**
 * Application service for online customer management (simplified).
 */
public class OnlineCustomerAppService {
    private final OnlineCustomerRepository onlineCustomerRepository;
    private final Logger logger;

    public OnlineCustomerAppService(OnlineCustomerRepository onlineCustomerRepository, Logger logger) {
        this.onlineCustomerRepository = onlineCustomerRepository;
        this.logger = logger;
    }

    /**
     * Register new online customer (simplified - plain text password).
     */
    public OnlineCustomer registerOnlineCustomer(String name, String email,
            String address, String plainPassword) {
        try {
            if (onlineCustomerRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already registered");
            }

            // Store password as plain text
            OnlineCustomer customer = new OnlineCustomer(
                    name, email, address, new Password(plainPassword));

            OnlineCustomer savedCustomer = onlineCustomerRepository.save(customer);
            logger.info("Registered online customer: " + email);
            return savedCustomer;

        } catch (Exception e) {
            logger.error("Error registering online customer", e);
            throw new RuntimeException("Failed to register online customer", e);
        }
    }

    /**
     * Authenticate online customer (simplified - plain text comparison).
     */
    public Optional<OnlineCustomer> authenticateCustomer(String email, String plainPassword) {
        try {
            Optional<OnlineCustomer> customerOpt = onlineCustomerRepository.findByEmail(email);

            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }

            OnlineCustomer customer = customerOpt.get();
            // Simple plain text password comparison
            boolean passwordValid = customer.getPassword().matches(plainPassword);

            return passwordValid ? Optional.of(customer) : Optional.empty();

        } catch (Exception e) {
            logger.error("Error authenticating online customer", e);
            return Optional.empty();
        }
    }
}
