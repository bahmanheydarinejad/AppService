package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.application.ui.Panel;
import org.springframework.stereotype.Repository;

@Repository
public interface PanelRepository extends CrudRepository<Panel, String> {

}
