package ir.appservice.common.search;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Single dynamic query filter rule representation.
 */
public class FilterCriterion implements Serializable {

    private String field;
    private FilterOperator operator;
    private Object value;
    private Object secondValue; // For BETWEEN
    private List<Object> values; // For IN / NOT_IN

    public FilterCriterion() {
        this.operator = FilterOperator.EQUALS;
    }

    public FilterCriterion(String field, FilterOperator operator, Object value) {
        this.field = field;
        this.operator = operator != null ? operator : FilterOperator.EQUALS;
        this.value = value;
    }

    public FilterCriterion(String field, FilterOperator operator, Object value, Object secondValue) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.secondValue = secondValue;
    }

    public FilterCriterion(String field, FilterOperator operator, List<Object> values) {
        this.field = field;
        this.operator = operator;
        this.values = values;
    }

    public static FilterCriterion equals(String field, Object value) {
        return new FilterCriterion(field, FilterOperator.EQUALS, value);
    }

    public static FilterCriterion contains(String field, Object value) {
        return new FilterCriterion(field, FilterOperator.CONTAINS, value);
    }

    public static FilterCriterion between(String field, Object start, Object end) {
        return new FilterCriterion(field, FilterOperator.BETWEEN, start, end);
    }

    public static FilterCriterion in(String field, List<Object> values) {
        return new FilterCriterion(field, FilterOperator.IN, values);
    }

    public static FilterCriterion isNull(String field) {
        return new FilterCriterion(field, FilterOperator.IS_NULL, null);
    }

    public static FilterCriterion isNotNull(String field) {
        return new FilterCriterion(field, FilterOperator.IS_NOT_NULL, null);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(Object secondValue) {
        this.secondValue = secondValue;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterCriterion that = (FilterCriterion) o;
        return Objects.equals(field, that.field) && operator == that.operator && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, operator, value);
    }

    @Override
    public String toString() {
        return "FilterCriterion{" +
                "field='" + field + '\'' +
                ", operator=" + operator +
                ", value=" + value +
                '}';
    }
}
