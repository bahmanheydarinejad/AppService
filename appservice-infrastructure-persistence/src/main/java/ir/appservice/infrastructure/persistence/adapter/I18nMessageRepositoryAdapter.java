package ir.appservice.infrastructure.persistence.adapter;

import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import ir.appservice.infrastructure.persistence.entity.I18nMessageJpaEntity;
import ir.appservice.infrastructure.persistence.repository.I18nMessageJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Driven Outbound Adapter: I18nMessageRepositoryAdapter.
 * Bridges I18nMessageRepositoryPort SPI to Spring Data JPA repository.
 */
@Component
public class I18nMessageRepositoryAdapter implements I18nMessageRepositoryPort {

    private final I18nMessageJpaRepository jpaRepository;

    public I18nMessageRepositoryAdapter(I18nMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<I18nMessage> findByKeyAndLocaleAndTenantId(String messageKey, String locale, String tenantId) {
        return jpaRepository.findByMessageKeyAndLocaleAndTenantId(messageKey, locale, tenantId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<I18nMessage> findByLocaleAndTenantId(String locale, String tenantId) {
        return jpaRepository.findByLocaleAndTenantId(locale, tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<I18nMessage> findByCategoryAndLocaleAndTenantId(String category, String locale, String tenantId) {
        return jpaRepository.findByCategoryAndLocaleAndTenantId(category, locale, tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public I18nMessage save(I18nMessage message) {
        I18nMessageJpaEntity entity = null;
        if (message.getId() != null) {
            entity = jpaRepository.findById(message.getId()).orElse(null);
        }
        if (entity == null) {
            entity = new I18nMessageJpaEntity(
                    message.getMessageKey(),
                    message.getLocale(),
                    message.getContent(),
                    message.getCategory(),
                    message.getTenantId()
            );
            if (message.getId() != null) {
                entity.setId(message.getId());
            }
        } else {
            entity.setMessageKey(message.getMessageKey());
            entity.setLocale(message.getLocale());
            entity.setMessageText(message.getContent());
            entity.setCategory(message.getCategory());
            entity.setTenantId(message.getTenantId());
        }

        entity.setVersion(message.getVersion());
        I18nMessageJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByKeyAndTenantId(String messageKey, String tenantId) {
        jpaRepository.deleteByMessageKeyAndTenantId(messageKey, tenantId);
    }

    private I18nMessage toDomain(I18nMessageJpaEntity entity) {
        I18nMessage message = new I18nMessage(
                entity.getId(),
                entity.getMessageKey(),
                entity.getLocale(),
                entity.getMessageText(),
                entity.getCategory(),
                entity.getTenantId()
        );
        message.setVersion(entity.getVersion());
        message.setCreatedAt(entity.getRegisteredDate());
        message.setUpdatedAt(entity.getUpdatedDate());
        return message;
    }
}
