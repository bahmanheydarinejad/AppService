package ir.appservice.core.domain.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Universal lifecycle state of domain entities.
 * Pure JDK 17 enum with state transition enforcement.
 */
public enum Status {
    ACTIVE,
    INACTIVE,
    PENDING,
    SUSPENDED,
    LOCKED,
    ARCHIVED,
    DELETED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isTerminal() {
        return this == DELETED;
    }

    /**
     * Enforces valid domain lifecycle transitions.
     *
     * @param target the desired state to transition to
     * @return true if transition is legally permitted
     */
    public boolean canTransitionTo(Status target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true;
        }
        return switch (this) {
            case PENDING -> EnumSet.of(ACTIVE, INACTIVE, DELETED).contains(target);
            case ACTIVE -> EnumSet.of(INACTIVE, SUSPENDED, LOCKED, ARCHIVED, DELETED).contains(target);
            case INACTIVE -> EnumSet.of(ACTIVE, ARCHIVED, DELETED).contains(target);
            case SUSPENDED -> EnumSet.of(ACTIVE, INACTIVE, DELETED).contains(target);
            case LOCKED -> EnumSet.of(ACTIVE, INACTIVE, SUSPENDED, DELETED).contains(target);
            case ARCHIVED -> EnumSet.of(ACTIVE, DELETED).contains(target);
            case DELETED -> false; // Terminal state, immutable
        };
    }
}
