package ir.appservice.common.search;

/**
 * Supported filter operators for dynamic Criteria specification construction.
 */
public enum FilterOperator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    BETWEEN,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL
}
