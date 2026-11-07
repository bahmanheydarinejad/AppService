package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.ApiKey;
import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Port (SPI): ApiKeyRepositoryPort.
 * Encapsulates persistence operations for API consumer keys.
 * Pure Java interface. Zero Spring or JPA annotations.
 */
public interface ApiKeyRepositoryPort {

    Optional<ApiKey> findByKeyId(String keyId);

    Optional<ApiKey> findById(String id);

    boolean existsByKeyId(String keyId);

    ApiKey save(ApiKey apiKey);

    void deleteById(String id);

    void deleteByKeyId(String keyId);

    List<ApiKey> findAllByTenantId(String tenantId);
}
