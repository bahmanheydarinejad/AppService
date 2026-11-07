package ir.appservice.infrastructure.i18n.message;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.core.domain.entity.I18nMessage;
import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Database-backed Dynamic MessageSource with local in-memory Caffeine L1 caching.
 * Resolves localized strings from database with sub-microsecond cache hit performance.
 */
public class DynamicMessageSource extends AbstractMessageSource {

    private final I18nMessageRepositoryPort repository;
    private final Cache<String, MessageFormat> caffeineCache;

    public DynamicMessageSource(I18nMessageRepositoryPort repository) {
        this(repository, 10_000, Duration.ofHours(24));
    }

    public DynamicMessageSource(I18nMessageRepositoryPort repository, long maxCacheSize, Duration ttl) {
        this.repository = repository;
        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build();
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (code == null) {
            return null;
        }

        Locale targetLocale = locale != null ? locale : Locale.getDefault();
        String tenantId = TenantContextHolder.getTenantId();
        String cacheKey = buildCacheKey(tenantId, targetLocale, code);

        // 1. Check Caffeine L1 Cache (Sub-microsecond resolution)
        MessageFormat cachedFormat = caffeineCache.getIfPresent(cacheKey);
        if (cachedFormat != null) {
            return cachedFormat;
        }

        // 2. Query Repository (Tenant-specific override first)
        MessageFormat resolvedFormat = queryDatabaseAndFormat(code, targetLocale, tenantId);
        if (resolvedFormat != null) {
            caffeineCache.put(cacheKey, resolvedFormat);
            return resolvedFormat;
        }

        // 3. Fallback to Global default (tenantId = null) if tenant was specified
        if (tenantId != null && !tenantId.isBlank()) {
            resolvedFormat = queryDatabaseAndFormat(code, targetLocale, null);
            if (resolvedFormat != null) {
                caffeineCache.put(cacheKey, resolvedFormat);
                return resolvedFormat;
            }
        }

        return null;
    }

    private MessageFormat queryDatabaseAndFormat(String code, Locale locale, String tenantId) {
        if (repository == null) {
            return null;
        }

        // Try exact language tag (e.g. "fa-IR")
        Optional<I18nMessage> messageOpt = repository.findByKeyAndLocaleAndTenantId(code, locale.toLanguageTag(), tenantId);
        if (messageOpt.isEmpty() && !locale.getLanguage().equalsIgnoreCase(locale.toLanguageTag())) {
            // Fallback to primary language code (e.g. "fa")
            messageOpt = repository.findByKeyAndLocaleAndTenantId(code, locale.getLanguage(), tenantId);
        }

        return messageOpt.map(msg -> createMessageFormat(msg.getContent(), locale)).orElse(null);
    }

    public void invalidate(String tenantId, String locale, String messageKey) {
        if (tenantId != null && locale != null && messageKey != null) {
            caffeineCache.invalidate(tenantId + ":" + locale + ":" + messageKey);
        } else if (messageKey != null) {
            // Invalidate all keys ending with the messageKey
            caffeineCache.asMap().keySet().removeIf(k -> k.endsWith(":" + messageKey));
        }
    }

    public void invalidateAll() {
        caffeineCache.invalidateAll();
    }

    public long getCacheSize() {
        return caffeineCache.estimatedSize();
    }

    public CacheStats getCacheStats() {
        return caffeineCache.stats();
    }

    private String buildCacheKey(String tenantId, Locale locale, String code) {
        String t = (tenantId != null && !tenantId.isBlank()) ? tenantId : "_global";
        return t + ":" + locale.toLanguageTag() + ":" + code;
    }
}
