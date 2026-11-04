package ir.appservice.model.entity.application;

import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.entity.application.ui.Menu;
import ir.appservice.model.entity.application.ui.Panel;
import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.entity.domain.NaturalPerson;
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
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Account extends BaseEntity {

    @Column(unique = true)
    @XmlElement
    protected String accountName;

    @XmlElement
    protected String password;

    @XmlElement
    protected String email;

    @XmlElement
    protected String mobileNumber;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected NaturalPerson naturalPerson;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Document avatar;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<ResetPasswordToken> resetPasswordTokens;

    @ManyToMany(mappedBy = "accounts", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<Role> roles;

    @Transient
    @XmlElement
    @XmlIDREF
    protected List<Panel> panels;

    @Transient
    @XmlElement
    @XmlIDREF
    protected List<Menu> accessMenus;

    public Account(String displayName, String accountName, String password, String email, String mobileNumber) {
        super(displayName);
        setAccountName(accountName);
        setPassword(password);
        setEmail(email);
        setMobileNumber(mobileNumber);
    }

    public Account(String displayName, String accountName, String password, String email, String mobileNumber, Document avatar) {
        this(displayName, accountName, password, email, mobileNumber);
        setAvatar(avatar);
    }

    public Account(String displayName, String accountName, String password, String email, String mobileNumber, NaturalPerson naturalPerson, Document avatar) {
        this(displayName, accountName, password, email, mobileNumber, avatar);
        setNaturalPerson(naturalPerson);
    }

    public Account(String displayName, String accountName, String password, String email, String mobileNumber, NaturalPerson naturalPerson, Document avatar, List<Role> roles) {
        this(displayName, accountName, password, email, mobileNumber, naturalPerson, avatar);
        setRoles(roles);
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
        if (this.roles != null) {
            this.roles.forEach(role -> {
                if (role.getAccounts() == null) {
                    role.setAccounts(new ArrayList<>());
                }
                if (!role.getAccounts().contains(this)) {
                    role.getAccounts().add(this);
                }
            });
        }
    }

    public void setResetPasswordToken(List<ResetPasswordToken> resetPasswordTokens) {
        this.resetPasswordTokens = resetPasswordTokens;
        if (this.resetPasswordTokens != null) {
            this.resetPasswordTokens.forEach(tokens -> tokens.setAccount(this));
        }
    }
}
