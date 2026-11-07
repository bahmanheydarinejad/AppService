package ir.appservice.ui.bean;

import ir.appservice.infrastructure.i18n.direction.DirectionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleBeanTest {

    private LocaleBean localeBean;

    @BeforeEach
    void setUp() {
        localeBean = new LocaleBean(new DirectionResolver());
    }

    @Test
    @DisplayName("STEP-8.3: LocaleBean defaults to English and LTR direction")
    void testDefaultLocale() {
        assertThat(localeBean.getLanguage()).isEqualTo("en");
        assertThat(localeBean.getDirection()).isEqualTo("ltr");
        assertThat(localeBean.isRtl()).isFalse();
    }

    @Test
    @DisplayName("STEP-8.3: LocaleBean switches to Persian and activates RTL direction")
    void testSwitchToPersian() {
        localeBean.switchLanguage("fa");
        assertThat(localeBean.getLanguage()).isEqualTo("fa");
        assertThat(localeBean.getDirection()).isEqualTo("rtl");
        assertThat(localeBean.isRtl()).isTrue();
    }

    @Test
    @DisplayName("STEP-8.3: LocaleBean switches to Arabic and activates RTL direction")
    void testSwitchToArabic() {
        localeBean.switchLanguage("ar");
        assertThat(localeBean.getLanguage()).isEqualTo("ar");
        assertThat(localeBean.getDirection()).isEqualTo("rtl");
        assertThat(localeBean.isRtl()).isTrue();
    }
}
