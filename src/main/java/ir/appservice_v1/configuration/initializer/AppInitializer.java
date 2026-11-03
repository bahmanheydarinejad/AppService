package ir.appservice_v1.configuration.initializer;

import ir.appservice_v1.model.entity.application.Account;
import ir.appservice_v1.model.entity.application.Role;
import ir.appservice_v1.model.entity.application.ui.Menu;
import ir.appservice_v1.model.entity.application.ui.Panel;
import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.entity.domain.NaturalPerson;
import ir.appservice_v1.model.service.AccountService;
import ir.appservice_v1.model.service.DocumentService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Component
@Setter
public class AppInitializer implements InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(AppInitializer.class);

    private Environment env;

    @Value("classpath:feeder.xml")
    private Resource feeder;
    @Value("classpath:images/userProfileAvatar.gif")
    private Resource avatarResource;
    @Value("classpath:initializer.xml")
    private Resource initializer;

    private AccountService accountService;
    private DocumentService documentService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AppInitializer(Environment env, AccountService accountService,
                          DocumentService documentService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.accountService = accountService;
        this.documentService = documentService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        if (!accountService.existsByAccountNameIgnoreCase("Admin")) {
            init();
        }
    }

    public void init() throws IOException {

        Document avatar = documentService.convertToDocument(avatarResource.getFile());

        NaturalPerson adminNP = new NaturalPerson("Admin", "Admin",
                "", "980000000000", new Date(), avatar);
        Account adminAccount = new Account("Admin", "Admin",
                bCryptPasswordEncoder.encode("Admin"), env.getProperty("spring.mail.username"), "980000000000",
                adminNP, avatar);

        NaturalPerson userNP = new NaturalPerson("User", "User",
                "", "980000000000", new Date(), avatar);
        Account userAccount = new Account("User", "User",
                bCryptPasswordEncoder.encode("User"), "User@" + env.getProperty("spring" +
                ".application" +
                ".domain"), "980000000000",
                userNP, avatar
        );

        Menu manageAccountPanelMenu = new Menu("Manage Accounts", "glyphicon " +
                "glyphicon-user",
                "/panels/crud_panels/account_panel/account_panel.xhtml", 10, null);

        Menu manageNaturalPersonPanelMenu = new Menu("Manage Natural Persons",
                "glyphicon glyphicon-user",
                "/panels/crud_panels/natural_person_panel/natural_person_panel.xhtml",
                20, null);

        Menu manageRolePanelMenu = new Menu("Manage Roles", "glyphicon glyphicon-queen",
                "/panels/crud_panels/role_panel/role_panel.xhtml", 30
                , null);

        Menu manageDocumentPanelMenu = new Menu("Manage Documents", "glyphicon glyphicon-book",
                "/panels/crud_panels/document_panel/document_panel.xhtml", 40, null);

        Menu managePanelPanelMenu = new Menu("Manage Panels", "glyphicon glyphicon-th-large",
                "/panels/crud_panels/panel_panel/panel_panel.xhtml", 50,
                null);

        Menu manageMenuPanelMenu = new Menu("Manage Menus", "glyphicon glyphicon-th-list",
                "/panels/crud_panels/menu_panel/menu_panel.xhtml", 60,
                null);

        Menu manageEMailPanelMenu = new Menu("Manage Emails", "glyphicon glyphicon-envelope",
                "/panels/crud_panels/email_panel/email_panel.xhtml", 70,
                null);

        Menu manageResetPasswordTokenPanelMenu = new Menu("Manage Reset Password Tokens",
                "glyphicon glyphicon-refresh",
                "/panels/crud_panels/reset_password_token_panel/reset_password_token_panel.xhtml"
                , 80,
                null);

        Menu configurationPanelMenu = new Menu("App Configuration", "glyphicon glyphicon-th-list",
                "/panels/configuration_panel.xhtml", 100, null);

        Menu homePanelMenu = new Menu("Home", "fa fa-cube",
                "/panels/home_panel.xhtml", 0, null);

        Menu manageEntitiesCategory = new Menu("Manage Entities", "glyphicon glyphicon-home", 100,
                null, Arrays.asList(new Menu[]{
                manageAccountPanelMenu,
                manageNaturalPersonPanelMenu,
                manageRolePanelMenu,
                manageDocumentPanelMenu,
                managePanelPanelMenu,
                manageMenuPanelMenu,
                manageEMailPanelMenu,
                manageResetPasswordTokenPanelMenu
        }));

        Menu configurationCategoryMenu = new Menu("Configuration",
                "glyphicon glyphicon-cog", 1000, null, Arrays.asList(new Menu[]{configurationPanelMenu}));

        Role adminRole = new Role("Administrator",
                new ArrayList<>(),
                Arrays.asList(new Panel[]{
                        homePanelMenu.getPanel(),
                        manageAccountPanelMenu.getPanel(),
                        manageNaturalPersonPanelMenu.getPanel(),
                        manageRolePanelMenu.getPanel(),
                        manageDocumentPanelMenu.getPanel(),
                        managePanelPanelMenu.getPanel(),
                        manageMenuPanelMenu.getPanel(),
                        configurationPanelMenu.getPanel(),
                        manageEMailPanelMenu.getPanel(),
                        manageResetPasswordTokenPanelMenu.getPanel()
                }));

        Role userRole = new Role("User",
                new ArrayList<>(),
                Arrays.asList(new Panel[]{homePanelMenu.getPanel()}));

        adminAccount.setRoles(Arrays.asList(new Role[]{adminRole}));
        userAccount.setRoles(Arrays.asList(new Role[]{userRole}));

        accountService.addAll(Arrays.asList(new Account[]{adminAccount, userAccount}));

    }

    @Override
    public void destroy() {

    }
}
