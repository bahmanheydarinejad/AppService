package ir.appservice.ui.bean;

import ir.appservice.infrastructure.i18n.direction.Direction;
import ir.appservice.infrastructure.i18n.direction.DirectionResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Locale;

/**
 * Backing bean managing locale selection and dynamic RTL/LTR direction mirroring.
 */
@Component("localeBean")
@SessionScope
public class LocaleBean implements Serializable {

    private final DirectionResolver directionResolver;
    private Locale currentLocale = Locale.ENGLISH;

    public LocaleBean() {
        this.directionResolver = new DirectionResolver();
    }

    public LocaleBean(DirectionResolver directionResolver) {
        this.directionResolver = directionResolver != null ? directionResolver : new DirectionResolver();
    }

    public void switchLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            this.currentLocale = Locale.ENGLISH;
            return;
        }
        String lang = languageCode.trim().toLowerCase();
        if (lang.startsWith("fa")) {
            this.currentLocale = new Locale("fa", "IR");
        } else if (lang.startsWith("ar")) {
            this.currentLocale = new Locale("ar");
        } else {
            this.currentLocale = Locale.ENGLISH;
        }
    }

    public String getLanguage() {
        return currentLocale.getLanguage();
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    public String getDirection() {
        return directionResolver.resolveHtmlDir(currentLocale);
    }

    public boolean isRtl() {
        return directionResolver.isRtl(currentLocale);
    }
}
