package ir.appservice.ui.bean;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Backing bean for Panel 5: Monitoring & Operations Telemetry Panel.
 */
@Component("opsMonitoringBean")
@SessionScope
public class OpsMonitoringBean implements Serializable {

    private double p50LatencyMs = 4.2;
    private double p95LatencyMs = 18.5;
    private double p99LatencyMs = 42.1;
    private int hikariActive = 3;
    private int hikariIdle = 17;
    private int hikariTotal = 20;
    private long heapUsedMb;
    private long heapMaxMb;
    private String systemLoad;

    public OpsMonitoringBean() {
        refreshMetrics();
    }

    public void refreshMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        this.heapUsedMb = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        this.heapMaxMb = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        this.systemLoad = load >= 0 ? String.format("%.2f", load) : "N/A";
    }

    // Getters
    public double getP50LatencyMs() { return p50LatencyMs; }
    public double getP95LatencyMs() { return p95LatencyMs; }
    public double getP99LatencyMs() { return p99LatencyMs; }
    public int getHikariActive() { return hikariActive; }
    public int getHikariIdle() { return hikariIdle; }
    public int getHikariTotal() { return hikariTotal; }
    public long getHeapUsedMb() { return heapUsedMb; }
    public long getHeapMaxMb() { return heapMaxMb; }
    public String getSystemLoad() { return systemLoad; }
}
