package edu.oel.tasknest;

/** The user-selected importance of a task.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public enum Priority {
    /** Lowest importance level. */
    LOW("Low", 1),
    /** Standard importance level. */
    MEDIUM("Medium", 2),
    /** Highest importance level. */
    HIGH("High", 3);

    private final String label;
    private final int rank;

    Priority(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    /** Returns the rank used to sort higher-priority tasks first.
     * @return the priority rank
     */
    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return label;
    }
}
