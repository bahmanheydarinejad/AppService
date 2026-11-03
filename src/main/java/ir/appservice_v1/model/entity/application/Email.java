package ir.appservice_v1.model.entity.application;

import ir.appservice_v1.model.entity.BaseEntity;
import ir.appservice_v1.model.entity.domain.Document;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Email extends BaseEntity {

    @XmlElement
    protected String mailFrom;

    @XmlElement
    protected String replyTo;

    @XmlElement
    protected String mailTo;

    @XmlElement
    protected String mailCc;

    @XmlElement
    protected String mailBcc;

    @XmlElement
    protected Date sentDate;

    @XmlElement
    protected String subject;

    @XmlElement
    protected String text;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @XmlElement
    @XmlIDREF
    protected List<Document> attachments;

    public List<String> getMailTo() {
        return mailTo != null ? Arrays.asList(mailTo.split(",")) : new ArrayList<>();
    }

    public List<String> getMailCc() {
        return mailCc != null ? Arrays.asList(mailCc.split(",")) : new ArrayList<>();
    }

    public List<String> getMailBcc() {
        return mailBcc != null ? Arrays.asList(mailBcc.split(",")) : new ArrayList<>();
    }

    public void addMailTo(String _to) {
        List<String> toList = getMailTo();
        toList.add(_to);
        this.mailTo = toList.toString()
                .replace("[", "")
                .replace(" ", "")
                .replace("]", "");
    }

    public void addMailCc(String _cc) {
        List<String> ccList = getMailCc();
        ccList.add(_cc);
        this.mailCc = ccList.toString()
                .replace("[", "")
                .replace(" ", "")
                .replace("]", "");
    }

    public void addMailBcc(String _bcc) {
        List<String> bccList = getMailBcc();
        bccList.add(_bcc);
        this.mailBcc = bccList.toString()
                .replace("[", "")
                .replace(" ", "")
                .replace("]", "");
    }
}
