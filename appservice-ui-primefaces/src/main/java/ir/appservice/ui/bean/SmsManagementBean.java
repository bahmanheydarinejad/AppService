package ir.appservice.ui.bean;

import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.CircuitState;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort;
import ir.appservice.infrastructure.platform.sms.SmartSmsRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for Panel 4: SMS Management Panel & Health Monitor.
 */
@Component("smsManagementBean")
@SessionScope
public class SmsManagementBean implements Serializable {

    private final SmartSmsRouter smsRouter;

    private String routingPolicy = "COST_OPTIMIZED";
    private String testRecipient = "+989121234567";
    private String testContent = "Test OTP: 549102";
    private String statusMessage;
    private SmsGatewayPort.SmsReceipt lastReceipt;

    @Autowired
    public SmsManagementBean(SmartSmsRouter smsRouter) {
        this.smsRouter = smsRouter;
    }

    public void updateRoutingPolicy(String policy) {
        if (policy != null) {
            this.routingPolicy = policy;
            this.statusMessage = "Routing policy switched to: " + policy;
        }
    }

    public void sendTestSms() {
        if (smsRouter != null && testRecipient != null && testContent != null) {
            try {
                SmsMessage msg = new SmsMessage(testRecipient, testContent, SmsProviderType.KAVENEGAR, "system");
                this.lastReceipt = smsRouter.sendSms(msg);
                this.statusMessage = "SMS sent via: " + lastReceipt.providerUsed() + ", delivered: " + lastReceipt.delivered();
            } catch (Exception e) {
                this.statusMessage = "SMS failed: " + e.getMessage();
            }
        }
    }

    public boolean isProviderHealthy(String providerName) {
        if (smsRouter != null && providerName != null) {
            try {
                SmsProviderType type = SmsProviderType.valueOf(providerName.toUpperCase());
                return smsRouter.isHealthy(type);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public String getCircuitState(String providerName) {
        if (smsRouter != null && providerName != null) {
            try {
                SmsProviderType type = SmsProviderType.valueOf(providerName.toUpperCase());
                CircuitState state = smsRouter.getCircuitState(type);
                return state != null ? state.name() : "CLOSED";
            } catch (Exception ignored) {
            }
        }
        return "CLOSED";
    }

    // Getters and Setters
    public String getRoutingPolicy() { return routingPolicy; }
    public void setRoutingPolicy(String routingPolicy) { this.routingPolicy = routingPolicy; }

    public String getTestRecipient() { return testRecipient; }
    public void setTestRecipient(String testRecipient) { this.testRecipient = testRecipient; }

    public String getTestContent() { return testContent; }
    public void setTestContent(String testContent) { this.testContent = testContent; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public SmsGatewayPort.SmsReceipt getLastReceipt() { return lastReceipt; }
}
