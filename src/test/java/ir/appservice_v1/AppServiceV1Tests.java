package ir.appservice_v1;

import ir.appservice_v1.controller.RestIndexController;
import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.application.Account;
import ir.appservice_v1.model.entity.application.Role;
import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.entity.domain.NaturalPerson;
import ir.appservice_v1.model.validator.AuthenticationValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.regex.Pattern;

public class AppServiceV1Tests {

    @Test
    public void testStarterApplicationIsolation() {
        SpringBootApplication runAnno = Run.class.getAnnotation(SpringBootApplication.class);
        Assert.assertNotNull("Run must have @SpringBootApplication annotation", runAnno);
        Assert.assertTrue("scanBasePackages must include ir.appservice_v1",
                runAnno.scanBasePackages().length > 0 && "ir.appservice_v1".equals(runAnno.scanBasePackages()[0]));

        SpringBootApplication appAnno = AppServiceV1Application.class.getAnnotation(SpringBootApplication.class);
        Assert.assertNotNull("AppServiceV1Application must have @SpringBootApplication annotation", appAnno);
        Assert.assertTrue("scanBasePackages must include ir.appservice_v1",
                appAnno.scanBasePackages().length > 0 && "ir.appservice_v1".equals(appAnno.scanBasePackages()[0]));
    }

    @Test
    public void testAccountEntityFeatures() {
        Account account = new Account();
        account.setDisplayName("Test User");
        account.setAccountName("testuser");
        account.setEmail("test@example.com");
        account.setPassword("Secret123");
        account.setMobileNumber("09123456789");

        NaturalPerson person = new NaturalPerson();
        person.setFirstName("Test");
        person.setLastName("User");
        account.setNaturalPerson(person);

        Role role = new Role("ROLE_USER");
        account.setRoles(Collections.singletonList(role));

        Assert.assertEquals("Test User", account.getDisplayName());
        Assert.assertEquals("testuser", account.getAccountName());
        Assert.assertEquals("test@example.com", account.getEmail());
        Assert.assertEquals(BaseEntity.ACTIVE_STATUS, account.getStatue());
        Assert.assertNotNull(account.getNaturalPerson());
        Assert.assertEquals("Test", account.getNaturalPerson().getFirstName());
        Assert.assertEquals(1, account.getRoles().size());
        Assert.assertEquals("ROLE_USER", account.getRoles().get(0).getDisplayName());
    }

    @Test
    public void testDocumentEntityFeatures() {
        byte[] payload = "Hello AppService V1".getBytes();
        Document document = new Document("readme.txt", "text/plain", "readme.txt", payload.length, payload);

        Assert.assertEquals("readme.txt", document.getDisplayName());
        Assert.assertEquals("text/plain", document.getType());
        Assert.assertEquals(payload.length, document.getSize());
        Assert.assertArrayEquals(payload, document.getData());
    }

    @Test
    public void testAuthenticationValidator() {
        AuthenticationValidator validator = new AuthenticationValidator();
        Assert.assertNotNull(validator);
    }

    @Test
    public void testRestIndexController() {
        RestIndexController controller = new RestIndexController();
        Account account = controller.index();
        Assert.assertNotNull(account);
        Assert.assertEquals("Bahman Heydari Nejad", account.getDisplayName());
        Assert.assertEquals("bahmanheydarinejad@gmail.com", account.getEmail());
    }

    @Test
    public void testRegexPatterns() {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        Assert.assertTrue(emailPattern.matcher("user@example.com").matches());
        Assert.assertFalse(emailPattern.matcher("invalid-email").matches());
    }
}
