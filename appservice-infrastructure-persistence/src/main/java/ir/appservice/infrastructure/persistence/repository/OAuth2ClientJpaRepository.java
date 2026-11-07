package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.OAuth2ClientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2ClientJpaRepository extends JpaRepository<OAuth2ClientJpaEntity, String>, JpaSpecificationExecutor<OAuth2ClientJpaEntity> {

    Optional<OAuth2ClientJpaEntity> findByClientIdAndDeleteDateIsNull(String clientId);

    boolean existsByClientIdAndDeleteDateIsNull(String clientId);

    List<OAuth2ClientJpaEntity> findAllByTenantIdAndDeleteDateIsNull(String tenantId);
}
