package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.OAuth2Client;
import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Port (SPI): OAuth2ClientRepositoryPort.
 * Encapsulates persistence operations for OAuth2 registered clients.
 * Pure Java interface. Zero Spring or JPA annotations.
 */
public interface OAuth2ClientRepositoryPort {

    Optional<OAuth2Client> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    OAuth2Client save(OAuth2Client client);

    void deleteByClientId(String clientId);

    List<OAuth2Client> findAllByTenantId(String tenantId);
}
