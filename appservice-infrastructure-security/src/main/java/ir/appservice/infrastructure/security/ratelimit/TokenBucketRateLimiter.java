package ir.appservice.infrastructure.security.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * High-performance, concurrency-safe Token Bucket Rate Limiter.
 * Regulates incoming request throughput per client/key without external dependencies.
 */
public class TokenBucketRateLimiter {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Attempts to consume specified number of tokens for the given key.
     *
     * @param key              Unique rate limit key (e.g., clientId, apiKey, or IP)
     * @param tokens           Number of tokens to consume (usually 1)
     * @param capacity         Maximum burst capacity of bucket
     * @param refillTokens     Number of tokens refilled per window
     * @param windowMillis     Duration of refill window in milliseconds
     * @return true if consumption allowed; false if throttled
     */
    public boolean tryConsume(String key, int tokens, int capacity, int refillTokens, long windowMillis) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, refillTokens, windowMillis));
        return bucket.tryConsume(tokens);
    }

    /**
     * Shorthand consumption using requests-per-minute configuration.
     *
     * @param key                  Unique rate limit key
     * @param requestsPerMinute    Allowed requests per minute
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean tryConsumePerMinute(String key, int requestsPerMinute) {
        int capacity = Math.max(1, requestsPerMinute);
        return tryConsume(key, 1, capacity, capacity, 60_000L);
    }

    public long getAvailableTokens(String key) {
        Bucket bucket = buckets.get(key);
        return bucket != null ? bucket.getAvailableTokens() : -1;
    }

    public void reset(String key) {
        buckets.remove(key);
    }

    public void clear() {
        buckets.clear();
    }

    private static class Bucket {
        private final long capacity;
        private final double refillRatePerMs;
        private double availableTokens;
        private long lastRefillTimestamp;

        public Bucket(long capacity, long refillTokens, long windowMillis) {
            this.capacity = capacity;
            this.refillRatePerMs = (double) refillTokens / (double) windowMillis;
            this.availableTokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume(int tokens) {
            refill();
            if (availableTokens >= tokens) {
                availableTokens -= tokens;
                return true;
            }
            return false;
        }

        public synchronized long getAvailableTokens() {
            refill();
            return (long) availableTokens;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > 0) {
                double tokensToAdd = elapsed * refillRatePerMs;
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }
}
