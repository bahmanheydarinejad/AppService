package ir.appservice_v1.model.repository;

import ir.appservice_v1.model.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface CrudRepository<T extends BaseEntity, ID extends Serializable> extends JpaRepository<T,
        ID>,
        JpaSpecificationExecutor<T> {

    boolean existsByDisplayNameIgnoreCase(String displayName);

    T findByDisplayName(String displayName);

    T findByDisplayNameIgnoreCase(String displayName);

    List<T> findAllByDeleteDateIsNull();

    Page<T> findAllByDeleteDateIsNull(Pageable pageable);

    List<T> findAllByDeleteDateIsNotNull();

    Page<T> findAllByDeleteDateIsNotNull(Pageable pageable);

    T getById(String id);
}
