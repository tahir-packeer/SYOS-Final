// File: src/main/java/org/syos/application/usecase/OnlineCustomerAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.OnlineCustomerRepository;
import org.syos.application.service.Logger;
import org.syos.domain.entity.OnlineCustomer;
import org.syos.domain.valueobject.Password;
import org.syos.infrastructure.util.PasswordHasher;
import java.util.Optional;

/**
 * Application service for online customer management.
 */
public class OnlineCustomerAppService {
    private final OnlineCustomerRepository onlineCustomerRepository;
    private final PasswordHasher passwordHasher;
    private final Logger logger;
    
    public OnlineCustomerAppService(OnlineCustomerRepository onlineCustomerRepository,
                                    PasswordHasher passwordHasher,
                                    Logger logger) {
        this.onlineCustomerRepository = onlineCustomerRepository;
        this.passwordHasher = passwordHasher;
        this.logger = logger;
    }
    
    /**
     * Register new online customer.
     */
    public OnlineCustomer registerOnlineCustomer(String name, String email, 
                                                 String address, String plainPassword) {
        try {
            if (onlineCustomerRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already registered");
            }
            
            String hashedPassword = passwordHasher.hash(plainPassword);
            OnlineCustomer customer = new OnlineCustomer(
                name, email, address, new Password(hashedPassword)
            );
            
            OnlineCustomer savedCustomer = onlineCustomerRepository.save(customer);
            logger.info("Registered online customer: " + email);
            return savedCustomer;
            
        } catch (Exception e) {
            logger.error("Error registering online customer", e);
            throw new RuntimeException("Failed to register online customer", e);
        }
    }
    
    /**
     * Authenticate online customer.
     */
    public Optional<OnlineCustomer> authenticateCustomer(String email, String plainPassword) {
        try {
            Optional<OnlineCustomer> customerOpt = onlineCustomerRepository.findByEmail(email);
            
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            
            OnlineCustomer customer = customerOpt.get();
            boolean passwordValid = passwordHasher.verify(
                plainPassword, 
                customer.getPassword().getHashedValue()
            );
            
            return passwordValid ? Optional.of(customer) : Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error authenticating online customer", e);
            return Optional.empty();
        }
    }
}
