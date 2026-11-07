package ir.appservice.common.context;

import ir.appservice.core.domain.valueobject.TraceId;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * ThreadLocal container propagating distributed tracing identifiers across thread boundaries.
 * Provides explicit resource lifecycle cleanup to guarantee zero memory leaks in thread pools.
 */
public final class TraceContextHolder {

    private static final ThreadLocal<String> CURRENT_TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SPAN_ID = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.trim().isEmpty()) {
            CURRENT_TRACE_ID.set(traceId.trim());
        } else {
            CURRENT_TRACE_ID.remove();
        }
    }

    public static String getTraceId() {
        return CURRENT_TRACE_ID.get();
    }

    public static String getTraceIdOrDefault() {
        String existing = CURRENT_TRACE_ID.get();
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        return TraceId.generate().getValue();
    }

    public static void setSpanId(String spanId) {
        if (spanId != null && !spanId.trim().isEmpty()) {
            CURRENT_SPAN_ID.set(spanId.trim());
        } else {
            CURRENT_SPAN_ID.remove();
        }
    }

    public static String getSpanId() {
        return CURRENT_SPAN_ID.get();
    }

    public static void clear() {
        CURRENT_TRACE_ID.remove();
        CURRENT_SPAN_ID.remove();
    }

    /**
     * Executes a runnable block with a bound trace context and guarantees deterministic cleanup.
     */
    public static void runWithTrace(String traceId, Runnable task) {
        String previous = CURRENT_TRACE_ID.get();
        try {
            setTraceId(traceId);
            task.run();
        } finally {
            if (previous != null) {
                CURRENT_TRACE_ID.set(previous);
            } else {
                CURRENT_TRACE_ID.remove();
            }
        }
    }

    /**
     * Executes a supplier with a bound trace context and guarantees deterministic cleanup.
     */
    public static <T> T supplyWithTrace(String traceId, Supplier<T> supplier) {
        String previous = CURRENT_TRACE_ID.get();
        try {
            setTraceId(traceId);
            return supplier.get();
        } finally {
            if (previous != null) {
                CURRENT_TRACE_ID.set(previous);
            } else {
                CURRENT_TRACE_ID.remove();
            }
        }
    }
}
