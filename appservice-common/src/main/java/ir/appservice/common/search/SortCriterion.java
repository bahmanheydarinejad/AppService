package ir.appservice.common.search;

import java.io.Serializable;
import java.util.Objects;

/**
 * Single sort field and direction specification.
 */
public class SortCriterion implements Serializable {

    private String field;
    private SortDirection direction;

    public SortCriterion() {
        this.direction = SortDirection.ASC;
    }

    public SortCriterion(String field, SortDirection direction) {
        this.field = field;
        this.direction = direction != null ? direction : SortDirection.ASC;
    }

    public static SortCriterion asc(String field) {
        return new SortCriterion(field, SortDirection.ASC);
    }

    public static SortCriterion desc(String field) {
        return new SortCriterion(field, SortDirection.DESC);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction != null ? direction : SortDirection.ASC;
    }

    public boolean isAscending() {
        return direction == SortDirection.ASC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortCriterion that = (SortCriterion) o;
        return Objects.equals(field, that.field) && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, direction);
    }

    @Override
    public String toString() {
        return field + " " + direction;
    }
}
