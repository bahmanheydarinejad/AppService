package ir.appservice.ui.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeSwitcherBeanTest {

    private ThemeSwitcherBean themeSwitcher;

    @BeforeEach
    void setUp() {
        themeSwitcher = new ThemeSwitcherBean();
    }

    @Test
    @DisplayName("STEP-8.2: ThemeSwitcherBean defaults to darkly and resolves CSS href")
    void testDefaultTheme() {
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("darkly");
        assertThat(themeSwitcher.getStylesheetHref()).isEqualTo("/css/themes/darkly.css");
        assertThat(themeSwitcher.getPrimaryColor()).isEqualTo("#375a7f");
    }

    @Test
    @DisplayName("STEP-8.2: ThemeSwitcherBean switches dynamically between all 6 themes")
    void testSwitchThemes() {
        themeSwitcher.switchTheme("adminlte");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("adminlte");
        assertThat(themeSwitcher.getPrimaryColor()).isEqualTo("#3c8dbc");

        themeSwitcher.switchTheme("flatly");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("flatly");

        themeSwitcher.switchTheme("cyborg");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("cyborg");

        themeSwitcher.switchTheme("pulse");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("pulse");

        themeSwitcher.switchTheme("emerald");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("emerald");
        assertThat(themeSwitcher.getPrimaryColor()).isEqualTo("#10b981");
    }

    @Test
    @DisplayName("STEP-8.2: ThemeSwitcherBean falls back to default on invalid theme")
    void testInvalidThemeFallback() {
        themeSwitcher.switchTheme("unknown_theme");
        assertThat(themeSwitcher.getCurrentTheme()).isEqualTo("darkly");
    }
}
