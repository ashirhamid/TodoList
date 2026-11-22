package edu.oel.tasknest;

import java.util.Objects;

/** A named group used to organize tasks.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public final class Category {
    private final String name;

    /** Creates a category after trimming its name.
     * @param name category name; must not be blank
     */
    public Category(String name) {
        String cleanName = Objects.requireNonNull(name, "name").trim();
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.name = cleanName;
    }

    /** Returns the category name.
     * @return category name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Category)) return false;
        Category category = (Category) other;
        return name.equalsIgnoreCase(category.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(java.util.Locale.ROOT).hashCode();
    }
}
