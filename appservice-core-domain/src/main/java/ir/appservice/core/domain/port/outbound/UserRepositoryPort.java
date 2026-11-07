package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Outbound Port (SPI): User Persistence Boundary Contract.
 * Pure Java interface defining persistence capabilities needed by domain.
 */
public interface UserRepositoryPort {

    Optional<User> findById(String id);

    Optional<User> findByUsername(String username, String tenantId);

    Optional<User> findByEmail(String email, String tenantId);

    boolean existsByUsername(String username, String tenantId);

    User save(User user);

    void deleteById(String id);

    List<User> findAll(String tenantId);

    long countActiveUsers(String tenantId);
}
