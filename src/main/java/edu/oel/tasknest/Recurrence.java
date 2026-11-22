package edu.oel.tasknest;

import java.time.LocalDate;

/** Recurring frequency for scheduled tasks (Algorithmic date scheduling).
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public enum Recurrence {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    private final String label;

    Recurrence(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate nextDate(LocalDate current) {
        if (current == null) return null;
        switch (this) {
            case DAILY: return current.plusDays(1);
            case WEEKLY: return current.plusWeeks(1);
            case MONTHLY: return current.plusMonths(1);
            default: return current;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
