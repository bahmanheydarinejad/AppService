package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.ApiKey;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.ApiKeyRepositoryPort;
import ir.appservice.infrastructure.persistence.entity.ApiKeyJpaEntity;
import ir.appservice.infrastructure.persistence.repository.ApiKeyJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Adapter: ApiKeyRepositoryAdapter.
 * Bridges ApiKeyRepositoryPort SPI to Spring Data JPA repository.
 */
@Component
public class ApiKeyRepositoryAdapter implements ApiKeyRepositoryPort {

    private final ApiKeyJpaRepository jpaRepository;

    public ApiKeyRepositoryAdapter(ApiKeyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findByKeyId(String keyId) {
        return jpaRepository.findByKeyIdAndDeleteDateIsNull(keyId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findById(String id) {
        return jpaRepository.findByIdAndDeleteDateIsNull(id)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKeyId(String keyId) {
        return jpaRepository.existsByKeyIdAndDeleteDateIsNull(keyId);
    }

    @Override
    @Transactional
    public ApiKey save(ApiKey apiKey) {
        ApiKeyJpaEntity entity = null;
        if (apiKey.getId() != null) {
            entity = jpaRepository.findById(apiKey.getId()).orElse(null);
        }
        if (entity == null) {
            entity = new ApiKeyJpaEntity(apiKey.getKeyId(), apiKey.getSecretHash(), apiKey.getName(), apiKey.getTenantId());
            if (apiKey.getId() != null) {
                entity.setId(apiKey.getId());
            }
        } else {
            entity.setKeyId(apiKey.getKeyId());
            entity.setSecretHash(apiKey.getSecretHash());
            entity.setName(apiKey.getName());
            entity.setTenantId(apiKey.getTenantId());
        }

        entity.setScopesFromSet(apiKey.getScopes());
        entity.setRateLimitPerMinute(apiKey.getRateLimitPerMinute());
        entity.setStatus(apiKey.getStatus() != null ? apiKey.getStatus().name() : "ACTIVE");
        entity.setExpiresAt(apiKey.getExpiresAt());

        ApiKeyJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeleteDate(Instant.now());
            entity.setStatus("DELETED");
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void deleteByKeyId(String keyId) {
        jpaRepository.findByKeyIdAndDeleteDateIsNull(keyId).ifPresent(entity -> {
            entity.setDeleteDate(Instant.now());
            entity.setStatus("DELETED");
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> findAllByTenantId(String tenantId) {
        return jpaRepository.findAllByTenantIdAndDeleteDateIsNull(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ApiKey toDomain(ApiKeyJpaEntity entity) {
        ApiKey apiKey = new ApiKey(
                entity.getId(),
                entity.getKeyId(),
                entity.getSecretHash(),
                entity.getName(),
                entity.getTenantId()
        );
        apiKey.setScopes(entity.getScopesAsSet());
        apiKey.setRateLimitPerMinute(entity.getRateLimitPerMinute());
        try {
            apiKey.setStatus(Status.valueOf(entity.getStatus()));
        } catch (Exception e) {
            apiKey.setStatus(Status.ACTIVE);
        }
        apiKey.setExpiresAt(entity.getExpiresAt());
        apiKey.setCreatedAt(entity.getRegisteredDate());
        apiKey.setUpdatedAt(entity.getUpdatedDate());
        return apiKey;
    }
}
