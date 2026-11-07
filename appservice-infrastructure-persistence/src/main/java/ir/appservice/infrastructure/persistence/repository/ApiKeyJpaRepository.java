package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.ApiKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyJpaEntity, String>, JpaSpecificationExecutor<ApiKeyJpaEntity> {

    Optional<ApiKeyJpaEntity> findByKeyIdAndDeleteDateIsNull(String keyId);

    Optional<ApiKeyJpaEntity> findByIdAndDeleteDateIsNull(String id);

    boolean existsByKeyIdAndDeleteDateIsNull(String keyId);

    List<ApiKeyJpaEntity> findAllByTenantIdAndDeleteDateIsNull(String tenantId);
}
