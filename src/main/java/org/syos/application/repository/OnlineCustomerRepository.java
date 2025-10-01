// File: src/main/java/org/syos/application/repository/OnlineCustomerRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.OnlineCustomer;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for online customers.
 */
public interface OnlineCustomerRepository {
    OnlineCustomer save(OnlineCustomer customer);
    Optional<OnlineCustomer> findById(Long customerId);
    Optional<OnlineCustomer> findByEmail(String email);
    List<OnlineCustomer> findAll();
    void update(OnlineCustomer customer);
    void delete(Long customerId);
    boolean existsByEmail(String email);
}