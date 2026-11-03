package ir.appservice_v1.model.entity.domain;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.application.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class NaturalPerson extends BaseEntity {

    @XmlElement
    protected String firstName;
    @XmlElement
    protected String lastName;
    @Size(min = 10, message = "Length is not correct!")
    @XmlElement
    protected String mobileNumber;
    @XmlElement
    protected Date birthDate;
    @OneToOne(mappedBy = "naturalPerson", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Account account;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Document avatar;

    public NaturalPerson(String displayName, String firstName, String lastName, String mobileNumber, Date birthDate, Account account, Document avatar) {
        this(displayName, firstName, lastName, mobileNumber, birthDate);
        setAccount(account);
        setAvatar(avatar);
    }

    public NaturalPerson(String displayName, String firstName, String lastName, String mobileNumber, Date birthDate, Document avatar) {
        this(displayName, firstName, lastName, mobileNumber, birthDate);
        setAvatar(avatar);
    }

    public NaturalPerson(String displayName, String firstName, String lastName, String mobileNumber, Date birthDate) {
        super(displayName);
        setFirstName(firstName);
        setLastName(lastName);
        setMobileNumber(mobileNumber);
        setBirthDate(birthDate);
    }

    public void setAccount(Account account) {
        this.account = account;
        if (this.account != null) {
            this.account.setNaturalPerson(this);
        }
    }
}
