package ir.appservice_v1.model.entity.application.ui;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.application.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Panel extends BaseEntity {

    @Lob
    @XmlElement
    protected String resourceLocation;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Menu menu;

    @ManyToMany(mappedBy = "panels", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<Role> roles;

    public Panel(String displayName, String resourceLocation) {
        super(displayName);
        setResourceLocation(resourceLocation);
    }

    public Panel(String displayName, String resourceLocation, int priority, String icon) {
        this(displayName, resourceLocation);
        setMenu(new Menu(displayName, icon, priority, Menu.PANEL_MENU));
    }

    public Panel(String displayName, String resourceLocation, Menu menu, List<Role> roles) {
        this(displayName, resourceLocation);
        setMenu(menu);
        setRoles(roles);
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
        if (this.roles != null) {
            this.roles.forEach(role -> {
                if (role.getPanels() == null) {
                    role.setPanels(new ArrayList<>());
                }
                if (!role.getPanels().contains(this)) {
                    role.getPanels().add(this);
                }
            });
        }
    }
}
