// File: src/main/java/org/syos/application/repository/CustomerRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for normal (counter) customers.
 */
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long customerId);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> searchByName(String name);
    void update(Customer customer);
    void delete(Long customerId);
}
