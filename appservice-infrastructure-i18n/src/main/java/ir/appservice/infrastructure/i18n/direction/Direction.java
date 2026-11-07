package ir.appservice.infrastructure.i18n.direction;

/**
 * Text and UI Layout Direction.
 */
public enum Direction {
    LTR("ltr"),
    RTL("rtl");

    private final String htmlAttribute;

    Direction(String htmlAttribute) {
        this.htmlAttribute = htmlAttribute;
    }

    public String getHtmlAttribute() {
        return htmlAttribute;
    }

    public boolean isRtl() {
        return this == RTL;
    }
}
