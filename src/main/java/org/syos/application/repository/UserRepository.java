// File: src/main/java/org/syos/application/repository/UserRepository.java
package org.syos.application.repository;

import org.syos.domain.entity.User;
import org.syos.domain.enums.UserRole;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Follows Dependency Inversion Principle - high-level code depends on this abstraction.
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    List<User> findByRole(UserRole role);
    void update(User user);
    void delete(Long userId);
    boolean existsByUsername(String username);
}