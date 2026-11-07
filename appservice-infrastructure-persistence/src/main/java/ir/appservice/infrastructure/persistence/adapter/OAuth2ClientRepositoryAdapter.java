package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.OAuth2Client;
import ir.appservice.core.domain.enums.Status;
import ir.appservice.core.domain.port.outbound.OAuth2ClientRepositoryPort;
import ir.appservice.infrastructure.persistence.entity.OAuth2ClientJpaEntity;
import ir.appservice.infrastructure.persistence.repository.OAuth2ClientJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Adapter: OAuth2ClientRepositoryAdapter.
 * Bridges OAuth2ClientRepositoryPort SPI to Spring Data JPA repository.
 */
@Component
public class OAuth2ClientRepositoryAdapter implements OAuth2ClientRepositoryPort {

    private final OAuth2ClientJpaRepository jpaRepository;

    public OAuth2ClientRepositoryAdapter(OAuth2ClientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OAuth2Client> findByClientId(String clientId) {
        return jpaRepository.findByClientIdAndDeleteDateIsNull(clientId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByClientId(String clientId) {
        return jpaRepository.existsByClientIdAndDeleteDateIsNull(clientId);
    }

    @Override
    @Transactional
    public OAuth2Client save(OAuth2Client client) {
        OAuth2ClientJpaEntity entity = jpaRepository.findById(client.getClientId())
                .orElseGet(OAuth2ClientJpaEntity::new);

        entity.setClientId(client.getClientId());
        entity.setClientSecretHash(client.getClientSecretHash());
        entity.setClientName(client.getClientName());
        entity.setScopesFromSet(client.getScopes());
        entity.setGrantTypesFromSet(client.getGrantTypes());
        entity.setRedirectUrisFromSet(client.getRedirectUris());
        entity.setRequirePkce(client.isRequirePkce());
        entity.setRateLimitPerMinute(client.getRateLimitPerMinute());
        entity.setStatus(client.getStatus() != null ? client.getStatus().name() : "ACTIVE");
        entity.setTenantId(client.getTenantId());

        OAuth2ClientJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByClientId(String clientId) {
        jpaRepository.findById(clientId).ifPresent(entity -> {
            entity.setDeleteDate(Instant.now());
            entity.setStatus("DELETED");
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<OAuth2Client> findAllByTenantId(String tenantId) {
        return jpaRepository.findAllByTenantIdAndDeleteDateIsNull(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    private OAuth2Client toDomain(OAuth2ClientJpaEntity entity) {
        OAuth2Client client = new OAuth2Client(
                entity.getClientId(),
                entity.getClientSecretHash(),
                entity.getClientName(),
                entity.getTenantId()
        );
        client.setScopes(entity.getScopesAsSet());
        client.setGrantTypes(entity.getGrantTypesAsSet());
        client.setRedirectUris(entity.getRedirectUrisAsSet());
        client.setRequirePkce(entity.isRequirePkce());
        client.setRateLimitPerMinute(entity.getRateLimitPerMinute());
        try {
            client.setStatus(Status.valueOf(entity.getStatus()));
        } catch (Exception e) {
            client.setStatus(Status.ACTIVE);
        }
        client.setCreatedAt(entity.getRegisteredDate());
        client.setUpdatedAt(entity.getUpdatedDate());
        return client;
    }
}
