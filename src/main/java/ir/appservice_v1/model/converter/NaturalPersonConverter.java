package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.domain.NaturalPerson;
import ir.appservice_v1.model.service.NaturalPersonService;
import org.springframework.stereotype.Component;

@Component
public class NaturalPersonConverter extends BaseConverter<NaturalPerson> {

    public NaturalPersonConverter(NaturalPersonService naturalPersonService) {
        super(NaturalPerson.class, naturalPersonService);
    }
}
