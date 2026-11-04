package ir.appservice.model.entity.application.ui;

import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.entity.application.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlRootElement;
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
