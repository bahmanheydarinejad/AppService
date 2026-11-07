package ir.appservice.infrastructure.i18n.event;

import com.google.gson.Gson;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis Pub/Sub Event Publisher for broadcasting translation cache evictions.
 */
public class RedisI18nEventPublisher implements I18nEventPublisher {

    public static final String I18N_EVICTION_TOPIC = "appservice:i18n:evict";

    private final StringRedisTemplate redisTemplate;
    private final Gson gson = new Gson();

    public RedisI18nEventPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publishInvalidation(I18nInvalidationEvent event) {
        if (redisTemplate != null && event != null) {
            String jsonPayload = gson.toJson(event);
            redisTemplate.convertAndSend(I18N_EVICTION_TOPIC, jsonPayload);
        }
    }
}
