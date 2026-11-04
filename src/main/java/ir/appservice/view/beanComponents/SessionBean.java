package ir.appservice.view.beanComponents;

import com.github.adminfaces.template.config.AdminConfig;
import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.entity.application.Account;
import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.service.AccountService;
import ir.appservice.model.service.DocumentService;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@Setter
@Getter
@Component
@SessionScope
public class SessionBean extends BaseBean {

    private HttpSession httpSession;
    private AccountService accountService;
    private DocumentService documentService;
    private AdminConfig adminConfig;
    private SessionRegistry sessionRegistry;

    private Account account = null;

    public SessionBean(HttpSession httpSession, AccountService accountService, DocumentService documentService, AdminConfig adminConfig, SessionRegistry sessionRegistry) {
        this.httpSession = httpSession;
        this.accountService = accountService;
        this.documentService = documentService;
        this.adminConfig = adminConfig;
        this.sessionRegistry = sessionRegistry;

        logger.trace("SessionBean account: " + account);
    }

    public void redirectAnonymousUsers(ComponentSystemEvent event) throws IOException {
        if (!isAuthenticated()) {
            logger.trace("Anonymous user detected => redirecting to /signin");
            FacesContext.getCurrentInstance().getExternalContext().redirect("/signin");
        }
    }

    public boolean isAuthenticated() {
        return account != null;
    }

    public boolean isAuthorized(BaseEntity object) {
        return accountService.isAuthorized(account, object);
    }

    public void edit() {
        try {
            account = accountService.edit(account);
            info("Update Profile Successful", "...", null);
        } catch (Exception e) {
            e.printStackTrace();
            fatal("Error!", e.getLocalizedMessage(), null);
        }
    }

    public void reload() {
        try {
            account = accountService.getWithRolesAndPanelsAndMenus(account.getId());
            logger.trace("Reload Profile Successful");
        } catch (Exception e) {
            e.printStackTrace();
            fatal("Error!", e.getLocalizedMessage(), null);
        }
    }

    public void uploadAvatar(FileUploadEvent event) {
        Document document = documentService.uploadDocument(event.getFile());
        account.setAvatar(document);
        accountService.edit(account);
        reload();
        info("Successful file upload", document.getDisplayName() + " is uploaded. Size (KB): " + document.getSize() / 1024f, null);
    }

    public void showSessions() {
        logger.trace("All Sessions:");
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            SessionInformation sessionInformation = (SessionInformation) principal;
            logger.warn(String.format("Principal: %s, Session Id: %s, Last request data: %s, Expired: %s", sessionInformation.getPrincipal(), sessionInformation.getSessionId(), sessionInformation.getLastRequest(), sessionInformation.isExpired()));
        }

    }

}