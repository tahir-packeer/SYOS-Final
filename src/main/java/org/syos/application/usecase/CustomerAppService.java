// File: src/main/java/org/syos/application/usecase/CustomerAppService.java
package org.syos.application.usecase;

import org.syos.application.repository.CustomerRepository;
import org.syos.application.service.Logger;
import org.syos.domain.entity.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Application service for normal customer management.
 */
public class CustomerAppService {
    private final CustomerRepository customerRepository;
    private final Logger logger;
    
    public CustomerAppService(CustomerRepository customerRepository, Logger logger) {
        this.customerRepository = customerRepository;
        this.logger = logger;
    }
    
    /**
     * Register new customer.
     */
    public Customer registerCustomer(String name, String phone) {
        try {
            Customer customer = new Customer(name, phone);
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Registered new customer: " + name);
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Error registering customer", e);
            throw new RuntimeException("Failed to register customer", e);
        }
    }
    
    /**
     * Find customer by phone.
     */
    public Optional<Customer> findByPhone(String phone) {
        try {
            return customerRepository.findByPhone(phone);
        } catch (Exception e) {
            logger.error("Error finding customer by phone", e);
            return Optional.empty();
        }
    }
    
    /**
     * Get all customers.
     */
    public List<Customer> getAllCustomers() {
        try {
            return customerRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving customers", e);
            throw new RuntimeException("Failed to retrieve customers", e);
        }
    }
}
