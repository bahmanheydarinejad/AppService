package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.I18nMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface I18nMessageJpaRepository extends JpaRepository<I18nMessageJpaEntity, String>, JpaSpecificationExecutor<I18nMessageJpaEntity> {

    Optional<I18nMessageJpaEntity> findByMessageKeyAndLocaleAndTenantId(String messageKey, String locale, String tenantId);

    List<I18nMessageJpaEntity> findByLocaleAndTenantId(String locale, String tenantId);

    List<I18nMessageJpaEntity> findByCategoryAndLocaleAndTenantId(String category, String locale, String tenantId);

    void deleteByMessageKeyAndTenantId(String messageKey, String tenantId);
}
