package ir.appservice.infrastructure.persistence;

import ir.appservice.common.search.FilterCriterion;
import ir.appservice.common.search.FilterOperator;
import ir.appservice.common.search.LogicalOperator;
import ir.appservice.common.search.SearchRequest;
import ir.appservice.infrastructure.persistence.entity.TenantJpaEntity;
import ir.appservice.infrastructure.persistence.repository.SpringDataTenantRepository;
import ir.appservice.infrastructure.persistence.specification.GenericSpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestPersistenceConfig.class)
@DisplayName("GenericSpecificationBuilder Criteria Predicates Tests")
class GenericSpecificationBuilderTest {

    @Autowired
    private SpringDataTenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        tenantRepository.deleteAll();

        TenantJpaEntity t1 = new TenantJpaEntity();
        t1.setTenantCode("corp_a");
        t1.setName("Acme Corporation");
        t1.setQuotaUsers(100);
        tenantRepository.save(t1);

        TenantJpaEntity t2 = new TenantJpaEntity();
        t2.setTenantCode("corp_b");
        t2.setName("Beta Logistics");
        t2.setQuotaUsers(250);
        tenantRepository.save(t2);

        TenantJpaEntity t3 = new TenantJpaEntity();
        t3.setTenantCode("corp_c");
        t3.setName("Cyberdyne Systems");
        t3.setQuotaUsers(500);
        tenantRepository.save(t3);
    }

    @Test
    @DisplayName("Verify EQUALS and CONTAINS operators with AND conjunction")
    void testEqualsAndContainsSpecification() {
        SearchRequest request = new SearchRequest("tenants", 0, 10)
                .addFilter(FilterCriterion.equals("tenantCode", "corp_a"))
                .addFilter(FilterCriterion.contains("name", "Acme"));

        Specification<TenantJpaEntity> spec = GenericSpecificationBuilder.toSpecification(request);
        List<TenantJpaEntity> result = tenantRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTenantCode()).isEqualTo("corp_a");
    }

    @Test
    @DisplayName("Verify GREATER_THAN_OR_EQUAL and BETWEEN range operators")
    void testNumericRangeOperators() {
        SearchRequest request = new SearchRequest("tenants", 0, 10)
                .addFilter(new FilterCriterion("quotaUsers", FilterOperator.GREATER_THAN_OR_EQUAL, 200));

        Specification<TenantJpaEntity> spec = GenericSpecificationBuilder.toSpecification(request);
        List<TenantJpaEntity> result = tenantRepository.findAll(spec);

        assertThat(result).hasSize(2); // corp_b (250) and corp_c (500)

        // Test BETWEEN
        SearchRequest betweenRequest = new SearchRequest("tenants", 0, 10)
                .addFilter(FilterCriterion.between("quotaUsers", 200, 300));

        List<TenantJpaEntity> betweenResult = tenantRepository.findAll(GenericSpecificationBuilder.toSpecification(betweenRequest));
        assertThat(betweenResult).hasSize(1);
        assertThat(betweenResult.get(0).getTenantCode()).isEqualTo("corp_b");
    }

    @Test
    @DisplayName("Verify IN operator with list of values")
    void testInOperator() {
        SearchRequest request = new SearchRequest("tenants", 0, 10)
                .addFilter(FilterCriterion.in("tenantCode", List.of("corp_a", "corp_c")));

        Specification<TenantJpaEntity> spec = GenericSpecificationBuilder.toSpecification(request);
        List<TenantJpaEntity> result = tenantRepository.findAll(spec);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Verify OR logical conjunction")
    void testOrConjunction() {
        SearchRequest request = new SearchRequest("tenants", 0, 10);
        request.setConjunction(LogicalOperator.OR);
        request.addFilter(FilterCriterion.equals("tenantCode", "corp_a"));
        request.addFilter(FilterCriterion.equals("tenantCode", "corp_b"));

        Specification<TenantJpaEntity> spec = GenericSpecificationBuilder.toSpecification(request);
        List<TenantJpaEntity> result = tenantRepository.findAll(spec);

        assertThat(result).hasSize(2);
    }
}
