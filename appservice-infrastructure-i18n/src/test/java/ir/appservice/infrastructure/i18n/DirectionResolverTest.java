package ir.appservice.infrastructure.i18n;

import ir.appservice.infrastructure.i18n.direction.Direction;
import ir.appservice.infrastructure.i18n.direction.DirectionResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Bidirectional Direction Resolver Tests (RTL/LTR & Locale Hierarchy)")
class DirectionResolverTest {

    private final DirectionResolver resolver = new DirectionResolver();

    @Test
    @DisplayName("Verify RTL languages return Direction.RTL and dir='rtl'")
    void testRtlLanguages() {
        // Persian (Farsi)
        assertThat(resolver.resolve(Locale.forLanguageTag("fa"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("fa-IR"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolveHtmlDir(Locale.forLanguageTag("fa-IR"))).isEqualTo("rtl");
        assertThat(resolver.isRtl(Locale.forLanguageTag("fa-IR"))).isTrue();

        // Arabic
        assertThat(resolver.resolve(Locale.forLanguageTag("ar"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("ar-SA"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("ar-EG"))).isEqualTo(Direction.RTL);

        // Hebrew
        assertThat(resolver.resolve(Locale.forLanguageTag("he"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("iw"))).isEqualTo(Direction.RTL);

        // Urdu & Kurdish & Pashto
        assertThat(resolver.resolve(Locale.forLanguageTag("ur"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("ku"))).isEqualTo(Direction.RTL);
        assertThat(resolver.resolve(Locale.forLanguageTag("ps"))).isEqualTo(Direction.RTL);
    }

    @Test
    @DisplayName("Verify LTR languages return Direction.LTR and dir='ltr'")
    void testLtrLanguages() {
        assertThat(resolver.resolve(Locale.ENGLISH)).isEqualTo(Direction.LTR);
        assertThat(resolver.resolve(Locale.US)).isEqualTo(Direction.LTR);
        assertThat(resolver.resolveHtmlDir(Locale.ENGLISH)).isEqualTo("ltr");
        assertThat(resolver.isRtl(Locale.ENGLISH)).isFalse();

        assertThat(resolver.resolve(Locale.FRENCH)).isEqualTo(Direction.LTR);
        assertThat(resolver.resolve(Locale.GERMAN)).isEqualTo(Direction.LTR);
        assertThat(resolver.resolve(Locale.forLanguageTag("es-ES"))).isEqualTo(Direction.LTR);
        assertThat(resolver.resolve(Locale.forLanguageTag("ru"))).isEqualTo(Direction.LTR);
    }

    @Test
    @DisplayName("Verify fallback hierarchy when locale is null or has country variant")
    void testFallbackHierarchy() {
        // Null locale falls back to defaultLocale (fa-IR -> RTL)
        assertThat(resolver.resolve(null)).isEqualTo(Direction.RTL);
        assertThat(resolver.resolveHtmlDir(null)).isEqualTo("rtl");

        // Custom default resolver with English default
        DirectionResolver englishDefaultResolver = new DirectionResolver(Locale.ENGLISH);
        assertThat(englishDefaultResolver.resolve(null)).isEqualTo(Direction.LTR);
        assertThat(englishDefaultResolver.resolveHtmlDir(null)).isEqualTo("ltr");
    }
}
