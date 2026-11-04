package ir.appservice;

import ir.appservice.controller.RestIndexController;
import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.entity.application.Account;
import ir.appservice.model.entity.application.Role;
import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.entity.domain.NaturalPerson;
import ir.appservice.model.validator.AuthenticationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class AppServiceTests {

    @Test
    public void testStarterApplicationIsolation() {
        SpringBootApplication runAnno = Run.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(runAnno, "Run must have @SpringBootApplication annotation");
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

        assertEquals("Test User", account.getDisplayName());
        assertEquals("testuser", account.getAccountName());
        assertEquals("test@example.com", account.getEmail());
        assertEquals(BaseEntity.ACTIVE_STATUS, account.getStatue());
        assertNotNull(account.getNaturalPerson());
        assertEquals("Test", account.getNaturalPerson().getFirstName());
        assertEquals(1, account.getRoles().size());
        assertEquals("ROLE_USER", account.getRoles().get(0).getDisplayName());
    }

    @Test
    public void testDocumentEntityFeatures() {
        byte[] payload = "Hello AppService".getBytes();
        Document document = new Document("readme.txt", "text/plain", "readme.txt", payload.length, payload);

        assertEquals("readme.txt", document.getDisplayName());
        assertEquals("text/plain", document.getType());
        assertEquals(payload.length, document.getSize());
        assertArrayEquals(payload, document.getData());
    }

    @Test
    public void testAuthenticationValidator() {
        AuthenticationValidator validator = new AuthenticationValidator();
        assertNotNull(validator);
    }

    @Test
    public void testRestIndexController() {
        RestIndexController controller = new RestIndexController();
        Account account = controller.index();
        assertNotNull(account);
        assertEquals("Bahman Heydari Nejad", account.getDisplayName());
        assertEquals("bahmanheydarinejad@gmail.com", account.getEmail());
    }

    @Test
    public void testRegexPatterns() {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        assertTrue(emailPattern.matcher("user@example.com").matches());
        assertFalse(emailPattern.matcher("invalid-email").matches());
    }
}
