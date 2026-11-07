package ir.appservice.infrastructure.persistence.repository;

import ir.appservice.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Role management with dynamic criteria specifications.
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String>, JpaSpecificationExecutor<RoleJpaEntity> {
}
