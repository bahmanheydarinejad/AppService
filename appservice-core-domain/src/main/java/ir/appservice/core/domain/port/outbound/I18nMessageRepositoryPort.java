package ir.appservice.core.domain.port.outbound;

import ir.appservice.core.domain.entity.I18nMessage;
import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Port (SPI): I18nMessageRepositoryPort.
 * Encapsulates persistence operations for dynamic database-backed localization messages.
 * Pure Java interface. Zero Spring or JPA annotations.
 */
public interface I18nMessageRepositoryPort {

    Optional<I18nMessage> findByKeyAndLocaleAndTenantId(String messageKey, String locale, String tenantId);

    List<I18nMessage> findByLocaleAndTenantId(String locale, String tenantId);

    List<I18nMessage> findByCategoryAndLocaleAndTenantId(String category, String locale, String tenantId);

    I18nMessage save(I18nMessage message);

    void deleteById(String id);

    void deleteByKeyAndTenantId(String messageKey, String tenantId);
}
