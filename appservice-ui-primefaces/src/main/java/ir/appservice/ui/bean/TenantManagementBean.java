package ir.appservice.ui.bean;

import ir.appservice.core.domain.enums.IsolationStrategy;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for Panel 6: Tenant Management & Isolation Wizard Panel.
 */
@Component("tenantManagementBean")
@SessionScope
public class TenantManagementBean implements Serializable {

    private final SpringDataTenantRepository tenantRepository;

    private List<TenantJpaEntity> tenants = new ArrayList<>();
    private String newTenantCode;
    private String newTenantName;
    private IsolationStrategy selectedStrategy = IsolationStrategy.SHARED_DB;
    private String statusMessage;

    @Autowired
    public TenantManagementBean(SpringDataTenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
        loadTenants();
    }

    public void loadTenants() {
        if (tenantRepository != null) {
            this.tenants = tenantRepository.findAll();
        }
    }

    public void provisionTenant() {
        if (newTenantCode != null && !newTenantCode.trim().isEmpty() && tenantRepository != null) {
            TenantJpaEntity tenant = new TenantJpaEntity();
            tenant.setTenantCode(newTenantCode.trim());
            tenant.setName(newTenantName != null ? newTenantName.trim() : newTenantCode.trim());
            tenant.setIsolationStrategy(selectedStrategy);
            tenantRepository.save(tenant);
            this.statusMessage = "Tenant provisioned: " + tenant.getTenantCode() + " (" + selectedStrategy + ")";
            this.newTenantCode = null;
            this.newTenantName = null;
            loadTenants();
        }
    }

    public void updateStrategy(TenantJpaEntity tenant, IsolationStrategy strategy) {
        if (tenant != null && strategy != null && tenantRepository != null) {
            tenant.setIsolationStrategy(strategy);
            tenantRepository.save(tenant);
            this.statusMessage = "Strategy updated to " + strategy + " for tenant: " + tenant.getTenantCode();
            loadTenants();
        }
    }

    // Getters and Setters
    public List<TenantJpaEntity> getTenants() { return tenants; }
    public String getNewTenantCode() { return newTenantCode; }
    public void setNewTenantCode(String newTenantCode) { this.newTenantCode = newTenantCode; }
    public String getNewTenantName() { return newTenantName; }
    public void setNewTenantName(String newTenantName) { this.newTenantName = newTenantName; }
    public IsolationStrategy getSelectedStrategy() { return selectedStrategy; }
    public void setSelectedStrategy(IsolationStrategy selectedStrategy) { this.selectedStrategy = selectedStrategy; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
