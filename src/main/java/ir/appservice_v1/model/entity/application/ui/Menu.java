package ir.appservice_v1.model.entity.application.ui;

import ir.appservice_v1.model.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Menu extends BaseEntity {

    public final static String PANEL_MENU = "PANEL_MENU";
    public final static String IN_PAGE_MENU = "IN_PAGE_MENU";
    public final static String CATEGORY_MENU = "CATEGORY_MENU";
    public static final Map<String, String> MENU_TYPES;

    static {
        MENU_TYPES = new HashMap();
        MENU_TYPES.put(PANEL_MENU, "Panel Menu");
        MENU_TYPES.put(IN_PAGE_MENU, "In Page Menu");
        MENU_TYPES.put(CATEGORY_MENU, "Category Menu");
    }

    @XmlElement
    protected int priority;

    @XmlElement
    protected String type;

    @XmlElement
    protected String icon;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Menu parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @XmlElement
    @XmlIDREF
    protected List<Menu> subMenus;

    @OneToOne(mappedBy = "menu", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @XmlElement
    @XmlIDREF
    protected Panel panel;

    public Menu(String displayName, String icon, int priority, Menu parent,
                List<Menu> subMenus) {
        this(displayName, icon, priority, Menu.CATEGORY_MENU);
        setParent(parent);
        setSubMenus(subMenus);
    }

    public Menu(String displayName, String icon, int priority, String type) {
        super(displayName);
        setIcon(icon);
        setPriority(priority);
        setType(type);
    }

    public Menu(String displayName, String icon,
                String panelResourceLocation, int priority, Menu parent) {
        this(displayName, icon, priority, Menu.PANEL_MENU);
        setPanel(new Panel(displayName, panelResourceLocation));
        setParent(parent);
    }

    public void setSubMenus(List<Menu> subMenus) {
        this.subMenus = subMenus;
        if (this.subMenus == null) {
            this.subMenus = new ArrayList<>();
        }
        this.subMenus.stream().forEach(menu -> menu.setParent(this));
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
        if (this.panel != null) {
            this.panel.setMenu(this);
        }
    }
}
