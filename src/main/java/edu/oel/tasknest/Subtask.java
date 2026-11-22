package edu.oel.tasknest;

import java.util.Objects;

/** A checklist item inside a parent task (Composite pattern component).
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public final class Subtask {
    private String title;
    private boolean completed;

    public Subtask(String title, boolean completed) {
        this.title = Objects.requireNonNull(title, "title").trim();
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title").trim();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        Subtask subtask = (Subtask) o;
        return completed == subtask.completed && Objects.equals(title, subtask.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, completed);
    }
}
