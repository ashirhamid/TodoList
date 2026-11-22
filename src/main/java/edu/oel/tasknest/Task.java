package edu.oel.tasknest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** A task record shown and managed by the application.
 * @author OEL Student Team (add team names before submission)
 */
public final class Task {
    private final UUID id;
    private final LocalDateTime createdAt;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;

    /** Creates a new task with a generated identifier and current creation time.
     * @param title task title
     * @param description optional description
     * @param category task category
     * @param priority task priority
     * @param status workflow status
     * @param dueDate optional due date
     */
    public Task(String title, String description, Category category,
                Priority priority, TaskStatus status, LocalDate dueDate) {
        this(UUID.randomUUID(), LocalDateTime.now(), title, description, category,
                priority, status, dueDate);
    }

    /** Restores a task from stored application data.
     * @param id stable task identifier
     * @param createdAt original creation time
     * @param title task title
     * @param description optional description
     * @param category task category
     * @param priority task priority
     * @param status workflow status
     * @param dueDate optional due date
     */
    public Task(UUID id, LocalDateTime createdAt, String title, String description,
                Category category, Priority priority, TaskStatus status, LocalDate dueDate) {
        this.id = Objects.requireNonNull(id, "id");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        update(title, description, category, priority, status, dueDate);
    }

    /** Updates all editable task fields.
     * @param title non-empty task title
     * @param description optional description
     * @param category task category
     * @param priority task priority
     * @param status workflow status
     * @param dueDate optional due date
     */
    public void update(String title, String description, Category category,
                       Priority priority, TaskStatus status, LocalDate dueDate) {
        String cleanTitle = Objects.requireNonNull(title, "title").trim();
        if (cleanTitle.isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        this.title = cleanTitle;
        this.description = Objects.requireNonNullElse(description, "").trim();
        this.category = Objects.requireNonNull(category, "category");
        this.priority = Objects.requireNonNull(priority, "priority");
        this.status = Objects.requireNonNull(status, "status");
        this.dueDate = dueDate;
    }

    /** Returns the stable task identifier.
     * @return stable task identifier
     */
    public UUID getId() { return id; }
    /** Returns the task title.
     * @return task title
     */
    public String getTitle() { return title; }
    /** Returns the task description.
     * @return task description
     */
    public String getDescription() { return description; }
    /** Returns the task category.
     * @return task category
     */
    public Category getCategory() { return category; }
    /** Returns the task priority.
     * @return task priority
     */
    public Priority getPriority() { return priority; }
    /** Returns the workflow status.
     * @return workflow status
     */
    public TaskStatus getStatus() { return status; }
    /** Returns the due date when one is set.
     * @return due date, or {@code null} when none is set
     */
    public LocalDate getDueDate() { return dueDate; }
    /** Returns the original creation timestamp.
     * @return original creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
