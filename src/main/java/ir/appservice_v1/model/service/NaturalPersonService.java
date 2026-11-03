package ir.appservice_v1.model.service;

import ir.appservice_v1.model.entity.domain.NaturalPerson;
import ir.appservice_v1.model.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;

@Service
@ApplicationScope
public class NaturalPersonService extends CrudService<NaturalPerson> {

    public NaturalPersonService(NaturalPersonRepository naturalPersonRepository) {
        super(naturalPersonRepository, NaturalPerson.class);
    }

    public NaturalPersonRepository getNaturalPersonRepository() {
        return (NaturalPersonRepository) this.crudRepository;
    }

    public List<NaturalPerson> listWithoutAccount() {
        return this.getNaturalPersonRepository().findByAccountIsNull();
    }
}
