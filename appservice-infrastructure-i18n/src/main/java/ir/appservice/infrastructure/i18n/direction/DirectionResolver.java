package ir.appservice.infrastructure.i18n.direction;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Bidirectional Direction Resolver.
 * Automatically resolves text and UI layout direction (RTL or LTR) based on Locale.
 * Persian (fa), Arabic (ar), Hebrew (he/iw), Urdu (ur), Yiddish (yi), Pashto (ps), and Kurdish (ku) resolve to RTL.
 */
@Component
public class DirectionResolver {

    private static final Set<String> RTL_LANGUAGES = Set.of(
            "fa",  // Persian (Farsi)
            "prs", // Dari
            "ar",  // Arabic
            "he",  // Hebrew
            "iw",  // Hebrew (legacy ISO)
            "ur",  // Urdu
            "yi",  // Yiddish
            "ji",  // Yiddish (legacy ISO)
            "ps",  // Pashto
            "ku",  // Kurdish
            "ug"   // Uyghur
    );

    private final Locale defaultLocale;

    public DirectionResolver() {
        this(Locale.forLanguageTag("fa-IR"));
    }

    public DirectionResolver(Locale defaultLocale) {
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.forLanguageTag("fa-IR");
    }

    /**
     * Resolves layout direction for the specified locale.
     */
    public Direction resolve(Locale locale) {
        Locale target = locale != null ? locale : defaultLocale;
        String language = target.getLanguage();
        if (language != null) {
            String normalized = language.trim().toLowerCase(Locale.ROOT);
            if (RTL_LANGUAGES.contains(normalized)) {
                return Direction.RTL;
            }
        }
        return Direction.LTR;
    }

    /**
     * Returns HTML attribute value 'rtl' or 'ltr'.
     */
    public String resolveHtmlDir(Locale locale) {
        return resolve(locale).getHtmlAttribute();
    }

    /**
     * Checks if the specified locale requires right-to-left alignment.
     */
    public boolean isRtl(Locale locale) {
        return resolve(locale).isRtl();
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }
}
