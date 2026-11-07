package ir.appservice.infrastructure.persistence.specification;

import ir.appservice.common.search.FilterCriterion;
import ir.appservice.common.search.FilterOperator;
import ir.appservice.common.search.LogicalOperator;
import ir.appservice.common.search.SearchRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal dynamic query specification builder.
 * Translates incoming JSON SearchRequest filters into type-safe JPA Criteria predicates.
 *
 * @param <T> Target JPA Entity type
 */
public class GenericSpecificationBuilder<T> {

    private final SearchRequest searchRequest;

    public GenericSpecificationBuilder(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    public static <T> Specification<T> toSpecification(SearchRequest searchRequest) {
        return new GenericSpecificationBuilder<T>(searchRequest).build();
    }

    public Specification<T> build() {
        return (root, query, criteriaBuilder) -> {
            if (searchRequest == null || searchRequest.getFilters() == null || searchRequest.getFilters().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (FilterCriterion criterion : searchRequest.getFilters()) {
                Predicate predicate = buildPredicate(root, criteriaBuilder, criterion);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            if (searchRequest.getConjunction() == LogicalOperator.OR) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicate(Root<T> root, CriteriaBuilder cb, FilterCriterion criterion) {
        if (criterion.getField() == null || criterion.getField().trim().isEmpty() || criterion.getOperator() == null) {
            return null;
        }

        Path<?> path = resolvePath(root, criterion.getField());
        FilterOperator operator = criterion.getOperator();
        Object rawValue = criterion.getValue();

        switch (operator) {
            case IS_NULL:
                return cb.isNull(path);

            case IS_NOT_NULL:
                return cb.isNotNull(path);

            case EQUALS:
                return cb.equal(path, convertValue(path.getJavaType(), rawValue));

            case NOT_EQUALS:
                return cb.notEqual(path, convertValue(path.getJavaType(), rawValue));

            case CONTAINS:
                return cb.like(cb.lower(path.as(String.class)), "%" + String.valueOf(rawValue).toLowerCase() + "%");

            case STARTS_WITH:
                return cb.like(cb.lower(path.as(String.class)), String.valueOf(rawValue).toLowerCase() + "%");

            case ENDS_WITH:
                return cb.like(cb.lower(path.as(String.class)), "%" + String.valueOf(rawValue).toLowerCase());

            case GREATER_THAN:
                return cb.greaterThan((Path<Comparable>) path, (Comparable) convertValue(path.getJavaType(), rawValue));

            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) convertValue(path.getJavaType(), rawValue));

            case LESS_THAN:
                return cb.lessThan((Path<Comparable>) path, (Comparable) convertValue(path.getJavaType(), rawValue));

            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) convertValue(path.getJavaType(), rawValue));

            case BETWEEN:
                Comparable start = (Comparable) convertValue(path.getJavaType(), rawValue);
                Comparable end = (Comparable) convertValue(path.getJavaType(), criterion.getSecondValue());
                return cb.between((Path<Comparable>) path, start, end);

            case IN:
                if (criterion.getValues() == null || criterion.getValues().isEmpty()) {
                    return cb.disjunction();
                }
                CriteriaBuilder.In<Object> inClause = cb.in(path);
                for (Object val : criterion.getValues()) {
                    inClause.value(convertValue(path.getJavaType(), val));
                }
                return inClause;

            case NOT_IN:
                if (criterion.getValues() == null || criterion.getValues().isEmpty()) {
                    return cb.conjunction();
                }
                CriteriaBuilder.In<Object> notInClause = cb.in(path);
                for (Object val : criterion.getValues()) {
                    notInClause.value(convertValue(path.getJavaType(), val));
                }
                return cb.not(notInClause);

            default:
                return null;
        }
    }

    private Path<?> resolvePath(Root<T> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertValue(Class<?> targetType, Object value) {
        if (value == null || targetType == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        String strValue = String.valueOf(value).trim();

        if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, strValue.toUpperCase());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(strValue);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(strValue);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(strValue);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(strValue);
        }
        if (targetType == Instant.class) {
            try {
                return Instant.parse(strValue);
            } catch (DateTimeParseException e) {
                return Instant.ofEpochMilli(Long.parseLong(strValue));
            }
        }
        return strValue;
    }
}
