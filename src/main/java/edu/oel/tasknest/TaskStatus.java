package edu.oel.tasknest;

/** The workflow state of a task.
 * @author OEL Student Team (add team names before submission)
 */
public enum TaskStatus {
    /** Task has not been started. */
    PENDING("Pending"),
    /** Task is being worked on. */
    IN_PROGRESS("In Progress"),
    /** Task is finished. */
    COMPLETED("Completed");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
