package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.domain.Document;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends CrudRepository<Document, String> {

}
