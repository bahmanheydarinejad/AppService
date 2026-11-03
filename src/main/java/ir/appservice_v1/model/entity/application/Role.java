package ir.appservice_v1.model.entity.application;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.application.ui.Panel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Role extends BaseEntity {

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<Account> accounts;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<Panel> panels;

    public Role(String displayName) {
        super(displayName);
    }

    public Role(String displayName, List<Account> accounts, List<Panel> panels) {
        super(displayName);
        setPanels(panels);
        setAccounts(accounts);
    }

    public Role(String displayName, List<Account> accounts) {
        super(displayName);
        setAccounts(accounts);
    }

}
