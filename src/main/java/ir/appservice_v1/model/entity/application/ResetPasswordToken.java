package ir.appservice_v1.model.entity.application;

import ir.appservice_v1.model.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class ResetPasswordToken extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected Account account;

    @XmlElement
    @XmlIDREF
    private String token;

    public ResetPasswordToken(String displayName) {
        super(displayName);
    }

    public ResetPasswordToken(String displayName, Account account) {
        super(displayName);
        setAccount(account);
    }

}
