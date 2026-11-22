package edu.oel.tasknest;

/** Command interface for Undo/Redo operations (GoF Command Pattern).
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public interface TaskCommand {
    /** Executes or re-executes the operation. */
    void execute();

    /** Undoes the operation, restoring previous state. */
    void undo();

    /** Human-readable short description for UI toasts and status logs. */
    String getDescription();
}
