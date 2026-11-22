package edu.oel.tasknest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/** Loads and saves tasks and categories in a local properties file.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
public final class TaskRepository {
    private static final String VERSION = "1";
    private final Path file;

    /** Creates a repository at the supplied path.
     * @param file persistent data file
     */
    public TaskRepository(Path file) {
        this.file = file;
    }

    /** Creates the default per-user data repository.
     * @return repository using the user's home directory
     */
    public static TaskRepository defaultRepository() {
        return new TaskRepository(Path.of(System.getProperty("user.home"), ".tasknest", "tasks.properties"));
    }

    /** Reads stored tasks and categories. A missing file starts with default categories.
     * @return application data
     * @throws IOException if the file cannot be read or contains invalid data
     */
    public AppData load() throws IOException {
        if (!Files.exists(file)) {
            return new AppData(new ArrayList<>(), defaultCategories());
        }
        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        String version = properties.getProperty("format.version", VERSION);
        if (!VERSION.equals(version)) {
            throw new IOException("Unsupported task data version: " + version);
        }

        List<Category> categories = new ArrayList<>();
        int categoryCount = parseCount(properties.getProperty("category.count", "0"), "category");
        for (int i = 0; i < categoryCount; i++) {
            categories.add(new Category(decode(required(properties, "category." + i + ".name"))));
        }
        if (categories.isEmpty()) categories.addAll(defaultCategories());

        List<Task> tasks = new ArrayList<>();
        int taskCount = parseCount(properties.getProperty("task.count", "0"), "task");
        for (int i = 0; i < taskCount; i++) {
            String prefix = "task." + i + ".";
            UUID id = UUID.fromString(required(properties, prefix + "id"));
            LocalDateTime createdAt = LocalDateTime.parse(required(properties, prefix + "createdAt"));
            String title = decode(required(properties, prefix + "title"));
            String description = decode(required(properties, prefix + "description"));
            Category category = new Category(decode(required(properties, prefix + "category")));
            if (!categories.contains(category)) categories.add(category);
            Priority priority = Priority.valueOf(required(properties, prefix + "priority"));
            TaskStatus status = TaskStatus.valueOf(required(properties, prefix + "status"));
            String due = required(properties, prefix + "dueDate");
            LocalDate dueDate = due.isEmpty() ? null : LocalDate.parse(due);
            Task task = new Task(id, createdAt, title, description, category, priority, status, dueDate);

            String recStr = properties.getProperty(prefix + "recurrence", "NONE");
            try {
                task.setRecurrence(Recurrence.valueOf(recStr));
            } catch (Exception ignored) {
                task.setRecurrence(Recurrence.NONE);
            }

            int subtaskCount = 0;
            String subCountStr = properties.getProperty(prefix + "subtask.count");
            if (subCountStr != null) {
                try { subtaskCount = Integer.parseInt(subCountStr); } catch (Exception ignored) {}
            }
            List<Subtask> subtasks = new ArrayList<>();
            for (int j = 0; j < subtaskCount; j++) {
                String subTitleEnc = properties.getProperty(prefix + "subtask." + j + ".title");
                if (subTitleEnc != null) {
                    String subTitle = decode(subTitleEnc);
                    boolean done = Boolean.parseBoolean(properties.getProperty(prefix + "subtask." + j + ".done", "false"));
                    subtasks.add(new Subtask(subTitle, done));
                }
            }
            task.setSubtasks(subtasks);
            tasks.add(task);
        }
        return new AppData(tasks, categories);
    }

    /** Saves all current tasks and categories with an atomic file replacement when supported.
     * @param tasks tasks to store
     * @param categories categories to store
     * @throws IOException if data cannot be written
     */
    public void save(List<Task> tasks, List<Category> categories) throws IOException {
        Files.createDirectories(file.getParent());
        Properties properties = new Properties();
        properties.setProperty("format.version", VERSION);
        properties.setProperty("category.count", Integer.toString(categories.size()));
        for (int i = 0; i < categories.size(); i++) {
            properties.setProperty("category." + i + ".name", encode(categories.get(i).getName()));
        }
        properties.setProperty("task.count", Integer.toString(tasks.size()));
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String prefix = "task." + i + ".";
            properties.setProperty(prefix + "id", task.getId().toString());
            properties.setProperty(prefix + "createdAt", task.getCreatedAt().toString());
            properties.setProperty(prefix + "title", encode(task.getTitle()));
            properties.setProperty(prefix + "description", encode(task.getDescription()));
            properties.setProperty(prefix + "category", encode(task.getCategory().getName()));
            properties.setProperty(prefix + "priority", task.getPriority().name());
            properties.setProperty(prefix + "status", task.getStatus().name());
            properties.setProperty(prefix + "dueDate", task.getDueDate() == null ? "" : task.getDueDate().toString());
            properties.setProperty(prefix + "recurrence", task.getRecurrence().name());
            properties.setProperty(prefix + "subtask.count", Integer.toString(task.getSubtasks().size()));
            for (int j = 0; j < task.getSubtasks().size(); j++) {
                Subtask sub = task.getSubtasks().get(j);
                properties.setProperty(prefix + "subtask." + j + ".title", encode(sub.getTitle()));
                properties.setProperty(prefix + "subtask." + j + ".done", Boolean.toString(sub.isCompleted()));
            }
        }

        Path temp = Files.createTempFile(file.getParent(), "tasks-", ".tmp");
        try {
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(temp), StandardCharsets.UTF_8)) {
                properties.store(writer, "TaskNest local data");
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static int parseCount(String value, String kind) throws IOException {
        try {
            int count = Integer.parseInt(value);
            if (count < 0 || count > 100_000) throw new NumberFormatException("out of range");
            return count;
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid " + kind + " count in saved data.", ex);
        }
    }

    private static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) throw new IOException("Missing saved data field: " + key);
        return value;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) throws IOException {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid encoded text in saved data.", ex);
        }
    }

    private static List<Category> defaultCategories() {
        return new ArrayList<>(List.of(new Category("Personal"), new Category("Study"), new Category("Work")));
    }

    /** Immutable pair of loaded lists.
     * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
     */
    public static final class AppData {
        private final List<Task> tasks;
        private final List<Category> categories;

        private AppData(List<Task> tasks, List<Category> categories) {
            this.tasks = tasks;
            this.categories = categories;
        }

        /** Returns the working list of tasks.
         * @return mutable working list of tasks
         */
        public List<Task> getTasks() { return tasks; }
        /** Returns the working list of categories.
         * @return mutable working list of categories
         */
        public List<Category> getCategories() { return categories; }
    }
}
