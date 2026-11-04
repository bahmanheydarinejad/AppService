package ir.appservice.model.entity.domain;

import ir.appservice.model.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
