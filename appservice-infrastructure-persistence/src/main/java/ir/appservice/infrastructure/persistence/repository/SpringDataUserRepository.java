package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User account management with soft-delete & tenant filtering.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String>, JpaSpecificationExecutor<UserJpaEntity> {

    @Query("SELECT u FROM UserJpaEntity u WHERE u.id = :id AND u.deleteDate IS NULL")
    Optional<UserJpaEntity> findActiveById(@Param("id") String id);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.username = :username AND u.tenantId = :tenantId AND u.deleteDate IS NULL")
    Optional<UserJpaEntity> findActiveByUsernameAndTenant(@Param("username") String username, @Param("tenantId") String tenantId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.tenantId = :tenantId AND u.deleteDate IS NULL")
    Optional<UserJpaEntity> findActiveByEmailAndTenant(@Param("email") String email, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE u.username = :username AND u.tenantId = :tenantId AND u.deleteDate IS NULL")
    boolean existsActiveByUsernameAndTenant(@Param("username") String username, @Param("tenantId") String tenantId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.tenantId = :tenantId AND u.deleteDate IS NULL")
    List<UserJpaEntity> findAllActiveByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE u.tenantId = :tenantId AND u.deleteDate IS NULL AND u.status = 'ACTIVE_STATUS'")
    long countActiveUsers(@Param("tenantId") String tenantId);
}
