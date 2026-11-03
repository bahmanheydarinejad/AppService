package ir.appservice_v1.model.entity.domain;

import ir.appservice_v1.model.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlRootElement
public class Document extends BaseEntity {

    @XmlElement
    protected String type;
    @XmlElement
    protected String description;
    @XmlElement
    protected long size;
    @Lob
    @NotNull
    @XmlElement
    @Lazy
    protected byte[] data;

    public Document(String displayName, String type, String description, long size,
                    @NotNull byte[] data) {
        super(displayName);
        setType(type);
        setDescription(description);
        setSize(size);
        setData(data);
    }
}
