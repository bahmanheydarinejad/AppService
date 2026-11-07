package ir.appservice.ui.bean;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Dynamic theming engine bean managing CSS custom property themes and instant runtime switching.
 * Supported themes: AdminLTE, Darkly, Flatly, Cyborg, Pulse, Emerald.
 */
@Component("themeSwitcherBean")
@SessionScope
public class ThemeSwitcherBean implements Serializable {

    public static final String DEFAULT_THEME = "darkly";

    private static final Map<String, String> THEME_PRIMARY_COLORS = Map.of(
            "adminlte", "#3c8dbc",
            "darkly", "#375a7f",
            "flatly", "#2c3e50",
            "cyborg", "#2a9fd6",
            "pulse", "#593196",
            "emerald", "#10b981"
    );

    private String currentTheme = DEFAULT_THEME;

    public void switchTheme(String theme) {
        if (theme != null && THEME_PRIMARY_COLORS.containsKey(theme.toLowerCase().trim())) {
            this.currentTheme = theme.toLowerCase().trim();
        } else {
            this.currentTheme = DEFAULT_THEME;
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        switchTheme(currentTheme);
    }

    public String getPrimaryColor() {
        return THEME_PRIMARY_COLORS.getOrDefault(currentTheme, "#375a7f");
    }

    public List<String> getAvailableThemes() {
        return List.of("adminlte", "darkly", "flatly", "cyborg", "pulse", "emerald");
    }

    public String getStylesheetHref() {
        return "/css/themes/" + currentTheme + ".css";
    }
}
