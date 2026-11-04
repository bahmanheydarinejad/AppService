package ir.appservice.view.beanComponents;

import ir.appservice.configuration.exception.InvalidEmailException;
import ir.appservice.configuration.exception.NotFoundEntityException;
import ir.appservice.model.service.AccountService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Setter
@Getter
@Component
@RequestScope
public class AuthenticationBean extends BaseBean {

    private String loginId = "";
    private String password = "";
    private AccountService accountService;

    public AuthenticationBean(AccountService accountService) {
        this.accountService = accountService;
    }

    public void authenticate() {
        if (loginId == null || loginId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            warn("Missing Credentials", "Please enter both Login ID and Password.", null);
            return;
        }

        try {
            accountService.authenticate(loginId.trim(), password);
            logger.trace(String.format("Authenticated %s and redirecting to /dashboard ...", loginId));

            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                ExternalContext externalContext = facesContext.getExternalContext();
                if (externalContext != null) {
                    Object req = externalContext.getRequest();
                    if (req instanceof HttpServletRequest httpReq) {
                        HttpSession session = httpReq.getSession(true);
                        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                SecurityContextHolder.getContext());
                    }
                    externalContext.getFlash().setKeepMessages(true);
                }
                info("Welcome :)", "You have successfully signed in.", null);
                if (externalContext != null) {
                    externalContext.redirect("/dashboard");
                }
            }
        } catch (AuthenticationException bce) {
            logger.warn("Authentication failed for {}: {}", loginId, bce.getMessage());
            error("Authentication Failed", bce.getMessage(), null);
        } catch (Exception e) {
            logger.error("Server error during login for {}", loginId, e);
            fatal("Server Error!", e.getMessage() != null ? e.getMessage() : "An unexpected server error occurred.", null);
        }
    }

    public void restPasswordSendMail() {
        if (loginId == null || loginId.trim().isEmpty()) {
            warn("Missing Email", "Please enter your email or login ID in the input box.", null);
            return;
        }

        try {
            accountService.resetPasswordSendMail(loginId.trim());
            info("Done", "Reset password link sent to mail account: " + loginId, null);
        } catch (NotFoundEntityException | InvalidEmailException e) {
            warn("Unsuccessful", e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error sending reset password mail for {}", loginId, e);
            fatal("Server Error", "If this error persisted, please contact administrator.", null);
        }
    }

}