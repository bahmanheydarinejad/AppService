package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.Permission;
import ir.appservice.core.domain.entity.Role;
import ir.appservice.core.domain.entity.User;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.UserRepositoryPort;
import ir.appservice.infrastructure.persistence.entity.PermissionJpaEntity;
import ir.appservice.infrastructure.persistence.entity.RoleJpaEntity;
import ir.appservice.infrastructure.persistence.entity.UserJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Driven Adapter implementing Outbound Port `UserRepositoryPort`.
 * Bridges domain User aggregate with Spring Data JPA persistence.
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;

    public UserRepositoryAdapter(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return userRepository.findActiveById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username, String tenantId) {
        return userRepository.findActiveByUsernameAndTenant(username.trim().toLowerCase(), tenantId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email, String tenantId) {
        return userRepository.findActiveByEmailAndTenant(email.trim().toLowerCase(), tenantId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username, String tenantId) {
        return userRepository.existsActiveByUsernameAndTenant(username.trim().toLowerCase(), tenantId);
    }

    @Override
    @Transactional
    public User save(User domainUser) {
        UserJpaEntity entity = userRepository.findActiveById(domainUser.getId())
                .orElseGet(UserJpaEntity::new);

        if (domainUser.getId() != null) {
            entity.setId(domainUser.getId());
        }
        entity.setUsername(domainUser.getUsername());
        entity.setPassword(domainUser.getPasswordHash());
        entity.setEmail(domainUser.getEmail());
        entity.setDisplayName(domainUser.getFirstName() + " " + domainUser.getLastName());
        entity.setMobileNumber(domainUser.getPhoneNumber());
        entity.setTenantId(domainUser.getTenantId());
        entity.setFailedLoginAttempts(domainUser.getFailedLoginAttempts());
        entity.setLastLoginAt(domainUser.getLastLoginAt());
        entity.setStatus(domainUser.getStatus() == Status.ACTIVE ? "ACTIVE_STATUS" : "DE_ACTIVE_STATUS");

        UserJpaEntity saved = userRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        userRepository.findActiveById(id).ifPresent(UserJpaEntity::softDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll(String tenantId) {
        return userRepository.findAllActiveByTenant(tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers(String tenantId) {
        return userRepository.countActiveUsers(tenantId);
    }

    private User toDomain(UserJpaEntity entity) {
        User user = new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getEmail(),
                entity.getTenantId()
        );
        user.setPhoneNumber(entity.getMobileNumber());
        user.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        user.setLastLoginAt(entity.getLastLoginAt());
        user.setCreatedAt(entity.getRegisteredDate());
        user.setUpdatedAt(entity.getUpdatedDate());
        if ("ACTIVE_STATUS".equalsIgnoreCase(entity.getStatus())) {
            user.setStatus(Status.ACTIVE);
        } else {
            user.setStatus(Status.SUSPENDED);
        }

        if (entity.getRoles() != null) {
            Set<Role> domainRoles = new HashSet<>();
            for (RoleJpaEntity r : entity.getRoles()) {
                Role domainRole = new Role(r.getId(), r.getName(), r.getDescription(), r.getTenantId());
                if (r.getPermissions() != null) {
                    for (PermissionJpaEntity p : r.getPermissions()) {
                        domainRole.addPermission(new Permission(p.getId(), p.getCode(), p.getDescription(), p.getModule()));
                    }
                }
                domainRoles.add(domainRole);
            }
            user.setRoles(domainRoles);
        }
        return user;
    }
}
