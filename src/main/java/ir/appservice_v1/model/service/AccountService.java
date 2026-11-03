package ir.appservice_v1.model.service;

import ir.appservice_v1.configuration.exception.DuplicateEntityException;
import ir.appservice_v1.configuration.exception.InvalidEmailException;
import ir.appservice_v1.configuration.exception.NotFoundEntityException;
import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.application.Account;
import ir.appservice_v1.model.entity.application.Email;
import ir.appservice_v1.model.entity.application.ResetPasswordToken;
import ir.appservice_v1.model.entity.application.Role;
import ir.appservice_v1.model.entity.application.ui.Menu;
import ir.appservice_v1.model.entity.application.ui.Panel;
import ir.appservice_v1.model.repository.AccountRepository;
import ir.appservice_v1.view.beanComponents.SessionBean;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.mail.MessagingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@ApplicationScope
public class AccountService extends CrudService<Account> implements AuthenticationProvider {

    private SessionBean sessionBean;
    private Pattern emailPattern;
    private Pattern mobilePattern;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private EMailService eMailService;
    private ResetPasswordTokenService resetPasswordTokenService;
    private Environment env;

    public AccountService(AccountRepository accountRepository, SessionBean sessionBean, Pattern emailPattern,
                          Pattern mobilePattern, BCryptPasswordEncoder bCryptPasswordEncoder,
                          EMailService eMailService,
                          ResetPasswordTokenService resetPasswordTokenService,
                          Environment env) {
        super(accountRepository, Account.class);
        this.sessionBean = sessionBean;
        this.emailPattern = emailPattern;
        this.mobilePattern = mobilePattern;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.eMailService = eMailService;
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.env = env;
    }

    public boolean existsByAccountNameIgnoreCase(String accountName) {
        return getAccountRepository().existsByAccountNameIgnoreCase(accountName);
    }

    public boolean isAuthorized(Account account, BaseEntity object) {
        List<Role> roles = getAccountRepository().getById(account.getId()).getRoles();
        if (object instanceof Panel && object != null) {
            for (Role role : roles) {
                if (role.getPanels().contains(object)) {
                    logger.trace(String.format("%s authorized to: %s", account, object));
                    return true;
                }
            }
        } else if (object instanceof Role && object != null) {
            account.getRoles().contains(object);
            logger.trace(String.format("%s authorized to: %s", account, object));
        }

        logger.trace(String.format("%s not authorized to: %s", account, object));
        return false;
    }

    public Account getWithRolesAndPanelsAndMenus(String id) {
        Account account = getAccountRepository().getOne(id);

        Set<Menu> rootAccountMenus = new HashSet();
        Set<Menu> availableAccountMenus = new HashSet();
        Set<Panel> accountPanels = new HashSet();

        account.getRoles().forEach(role -> role.getPanels().stream().forEach(panel -> {
            Menu temp = panel.getMenu();
            while (temp != null) {
                if (temp.getParent() == null) {
                    rootAccountMenus.add(temp);
                } else {
                    availableAccountMenus.add(temp);
                }

                temp = temp.getParent();
            }
            accountPanels.add(panel);
        }));

        account.setPanels(accountPanels.stream().sorted((o1, o2) -> {
                    if (o1.getMenu() == null) {
                        return -1;
                    }
                    if (o2.getMenu() == null) {
                        return 1;
                    }
                    return o1.getMenu().getPriority() > o2.getMenu().getPriority() ? 1 :
                            o1.getMenu().getPriority() < o2.getMenu().getPriority() ? -1 : 0;
                }).collect(Collectors.toList())
        );

        rootAccountMenus.forEach(menu -> addChildMenu(menu, availableAccountMenus));

        account.setAccessMenus(rootAccountMenus.stream().sorted(Comparator.comparingInt(Menu::getPriority)).collect(Collectors.toList()));

        account.getAccessMenus().forEach(menu -> printMenus(menu, 0));

        logger.trace("Account Roles: {}", account.getRoles());
        logger.trace("Account Panels: {}", account.getPanels());
        logger.trace("Root Account Menus: {}", account.getAccessMenus());
        logger.trace("All Account Menus: {}", availableAccountMenus);

        return account;
    }

    private void addChildMenu(Menu p, Set<Menu> availableChild) {
        Set<Menu> removeItems = new HashSet();
        p.setSubMenus(new ArrayList());
        availableChild.forEach(m -> {
            Menu parent = m.getParent();
            if (p.equals(parent)) {
                logger.trace("{} is parent of {}", p, m);
                p.getSubMenus().add(m);
                removeItems.add(m);
            }
        });

        logger.trace("removeItems: {}", removeItems);
        availableChild.removeAll(removeItems);
        logger.trace("availableChild: {}", availableChild);

        if (!availableChild.isEmpty()) {
            p.getSubMenus().forEach(subMenu -> addChildMenu(subMenu, availableChild));
        }

        p.setSubMenus(p.getSubMenus().stream().sorted(Comparator.comparingInt(Menu::getPriority)).collect(Collectors.toList()));

    }

    private void printMenus(Menu menu, int level) {
        logger.trace("{}: {}", StringUtils.repeat("\t\t", level), menu);
        menu.getSubMenus().forEach(sub -> printMenus(sub, level + 1));
    }

    public AccountRepository getAccountRepository() {
        return (AccountRepository) this.crudRepository;
    }

    @Override
    public Account add(Account account) {
        if (account.getPassword() == null || account.getPassword().trim().isEmpty()) {
            account.setPassword(RandomStringUtils.randomAlphabetic(8));
        }

        if (getAccountRepository().existsByAccountNameIgnoreCase(account.getAccountName())) {
            throw new DuplicateEntityException(String.format("Account %s already exist!", account.getAccountName()));
        }

        return super.add(account);
    }

    public void authenticate(String loginId, String password) throws AuthenticationException {
        logger.trace(String.format("Authenticating: %s, password: %s", loginId, password));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);
        Authentication auth = authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public void resetPasswordSendMail(String email) throws NotFoundEntityException, MessagingException {
        Account account = getAccountRepository().findByEmailIgnoreCase(email);
        if (account == null) {
            throw new NotFoundEntityException(
                    String.format("Account with such email \"%s\" was not found!", email));
        }

        if (!emailPattern.matcher(account.getEmail()).matches()) {
            throw new InvalidEmailException(
                    String.format("Seems email \"%s\" is not valid! Please communicate with " +
                                    "\"%s\""
                            , account.getEmail(), env.getProperty("spring.mail.username")));
        }

        ResetPasswordToken rpt = new ResetPasswordToken();
        rpt.setAccount(account);
        rpt.setToken(RandomStringUtils.randomAlphabetic(64));
        rpt.setDisplayName("Reset Password Token");
        resetPasswordTokenService.add(rpt);

        Email resetPassword = new Email();
        resetPassword.setMailFrom(env.getProperty("spring.mail.username"));
        resetPassword.setMailTo(account.getEmail());
        resetPassword.setSubject("Reset Password");
        resetPassword.setText(
                String.format("Hi dear %s, you can go through <a href='%s'>this</a> link to reset " +
                                "your password.", account.getAccountName(),
                        "http://localhost:8001/resetPassword?token=" + rpt.getToken())
        );
        eMailService.add(resetPassword);
        eMailService.sendMail(resetPassword);
        edit(account);
    }

    public Account loginByAny(String loginId, String password) throws UsernameNotFoundException {

        Account account;
        if (emailPattern.matcher(loginId).matches()) {
            account = getAccountRepository().findByEmailIgnoreCase(loginId);
            logger.info("Check by email ... {}", account);

        } else if (mobilePattern.matcher(loginId).matches()) {
            account = getAccountRepository().findByMobileNumber(loginId);
            logger.info("Check by mobile ... {}", account);

        } else {
            account = getAccountRepository().findByAccountNameIgnoreCase(loginId);
            logger.info("Check by account name ... {}", account);
        }

        if (account != null && bCryptPasswordEncoder.matches(password, account.getPassword())) {

            logger.info("Authenticated account: " + account);

            return getWithRolesAndPanelsAndMenus(account.getId());

        } else {
            logger.warn("Account '{}' not found!", loginId);
            throw new UsernameNotFoundException("Try again with correct Login ID and Password!");
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = authentication.getCredentials().toString();

        Account loginAccount = loginByAny(loginId, password);
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        loginAccount.getRoles().forEach(role -> {
            logger.trace("Account Role: " + role.getDisplayName());
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getDisplayName()));
        });

        sessionBean.setAccount(loginAccount);

        return new UsernamePasswordAuthenticationToken(loginAccount.getAccountName(), loginAccount.getPassword(), grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}