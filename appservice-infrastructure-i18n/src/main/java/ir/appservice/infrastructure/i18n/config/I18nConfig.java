package ir.appservice.infrastructure.i18n.config;

import ir.appservice.core.domain.port.outbound.I18nMessageRepositoryPort;
import ir.appservice.infrastructure.i18n.direction.DirectionResolver;
import ir.appservice.infrastructure.i18n.event.I18nCacheInvalidator;
import ir.appservice.infrastructure.i18n.event.I18nEventPublisher;
import ir.appservice.infrastructure.i18n.event.RedisI18nEventPublisher;
import ir.appservice.infrastructure.i18n.message.DynamicMessageSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Spring Auto-Configuration for Dynamic i18n, Caffeine L1 cache, Direction Resolver, and Redis eviction.
 */
@Configuration
public class I18nConfig {

    @Bean
    @ConditionalOnMissingBean
    public DirectionResolver directionResolver() {
        return new DirectionResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicMessageSource dynamicMessageSource(ObjectProvider<I18nMessageRepositoryPort> repositoryProvider) {
        return new DynamicMessageSource(repositoryProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nEventPublisher i18nEventPublisher(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return new RedisI18nEventPublisher(redisTemplate);
        }
        // Fallback no-op publisher if Redis is not configured in standalone mode
        return event -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nCacheInvalidator i18nCacheInvalidator(DynamicMessageSource messageSource, I18nEventPublisher publisher) {
        return new I18nCacheInvalidator(messageSource, publisher);
    }
}
