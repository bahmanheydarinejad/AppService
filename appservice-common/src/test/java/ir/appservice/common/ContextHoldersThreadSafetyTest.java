package ir.appservice.common;

import ir.appservice.common.context.TenantContextHolder;
import ir.appservice.common.context.TraceContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ThreadLocal Context Holders Safety & Concurrency Tests")
class ContextHoldersThreadSafetyTest {

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("TraceContextHolder: verify basic set, get, auto-default, and clear")
    void testTraceContextLifecycle() {
        TraceContextHolder.clear();
        assertThat(TraceContextHolder.getTraceId()).isNull();

        String defaultTrace = TraceContextHolder.getTraceIdOrDefault();
        assertThat(defaultTrace).startsWith("tr-");

        TraceContextHolder.setTraceId("tr-explicit-999");
        TraceContextHolder.setSpanId("span-10");
        assertThat(TraceContextHolder.getTraceId()).isEqualTo("tr-explicit-999");
        assertThat(TraceContextHolder.getSpanId()).isEqualTo("span-10");

        TraceContextHolder.clear();
        assertThat(TraceContextHolder.getTraceId()).isNull();
        assertThat(TraceContextHolder.getSpanId()).isNull();
    }

    @Test
    @DisplayName("TenantContextHolder: verify basic set, get, auto-default, and clear")
    void testTenantContextLifecycle() {
        TenantContextHolder.clear();
        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(TenantContextHolder.getTenantIdOrDefault()).isEqualTo("default");

        TenantContextHolder.setTenantId("Tenant_Acme");
        assertThat(TenantContextHolder.getTenantId()).isEqualTo("tenant_acme");

        TenantContextHolder.clear();
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    @Test
    @DisplayName("Scoped execution: runWithTrace and runWithTenant restore previous state")
    void testScopedExecutionRestoration() {
        TenantContextHolder.setTenantId("tenant_original");
        TraceContextHolder.setTraceId("trace_original");

        TenantContextHolder.runWithTenant("tenant_temporary", () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo("tenant_temporary");
        });
        assertThat(TenantContextHolder.getTenantId()).isEqualTo("tenant_original");

        TraceContextHolder.runWithTrace("trace_temporary", () -> {
            assertThat(TraceContextHolder.getTraceId()).isEqualTo("trace_temporary");
        });
        assertThat(TraceContextHolder.getTraceId()).isEqualTo("trace_original");
    }

    @Test
    @DisplayName("Concurrency: verify ThreadLocal isolation across multiple concurrent worker threads")
    void testThreadLocalIsolationUnderConcurrency() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean allPassed = new AtomicBoolean(true);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String threadTenant = "tenant_" + index;
                    String threadTrace = "trace_" + index;

                    TenantContextHolder.setTenantId(threadTenant);
                    TraceContextHolder.setTraceId(threadTrace);

                    Thread.sleep(20);

                    if (!threadTenant.equals(TenantContextHolder.getTenantId())) {
                        allPassed.set(false);
                    }
                    if (!threadTrace.equals(TraceContextHolder.getTraceId())) {
                        allPassed.set(false);
                    }
                } catch (Exception e) {
                    allPassed.set(false);
                } finally {
                    TenantContextHolder.clear();
                    TraceContextHolder.clear();
                    latch.countDown();
                }
            });
        }

        boolean finishedInTime = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finishedInTime).isTrue();
        assertThat(allPassed.get()).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(TraceContextHolder.getTraceId()).isNull();
    }
}
