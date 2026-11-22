package edu.oel.tasknest;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Dialog for creating or editing a task.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
final class TaskEditorDialog {
    private static final java.awt.Color INK = new java.awt.Color(37, 57, 86);
    private static final java.awt.Color BLUE = new java.awt.Color(74, 105, 151);
    private static final java.awt.Color FIELD = new java.awt.Color(255, 248, 233);

    private TaskEditorDialog() { }

    /** Opens the editor and returns the entered values, or {@code null} if canceled.
     * @param parent owner component
     * @param existing task to edit, or {@code null} to create
     * @param categories available categories
     * @return validated draft, or {@code null} when canceled
     */
    static Draft show(Component parent, Task existing, List<Category> categories) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, existing == null ? "Create a task" : "Edit task",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(480, 540));
        dialog.setResizable(false);

        JPanel root = new ColorWashPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        JLabel heading = new JLabel(existing == null ? "A small step, captured." : "Refine your task");
        heading.setFont(new java.awt.Font("Georgia", java.awt.Font.BOLD, 22));
        heading.setForeground(INK);
        root.add(heading, BorderLayout.NORTH);

        JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
        styleInput(title);
        JTextArea description = new JTextArea(existing == null ? "" : existing.getDescription(), 5, 20);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.PLAIN, 13));
        description.setBackground(FIELD);
        description.setBorder(BorderFactory.createEmptyBorder(8, 9, 8, 9));
        JComboBox<String> category = new JComboBox<>();
        category.setEditable(true);
        for (Category item : categories) category.addItem(item.getName());
        if (existing != null && !categories.contains(existing.getCategory())) category.addItem(existing.getCategory().getName());
        category.setSelectedItem(existing == null ? "Personal" : existing.getCategory().getName());
        JComboBox<Priority> priority = new JComboBox<>(Priority.values());
        priority.setSelectedItem(existing == null ? Priority.MEDIUM : existing.getPriority());
        JComboBox<TaskStatus> status = new JComboBox<>(TaskStatus.values());
        status.setSelectedItem(existing == null ? TaskStatus.PENDING : existing.getStatus());
        JTextField dueDate = new JTextField(existing == null || existing.getDueDate() == null
                ? "" : existing.getDueDate().toString());
        styleInput(dueDate);
        JComboBox<Recurrence> recurrence = new JComboBox<>(Recurrence.values());
        recurrence.setSelectedItem(existing == null ? Recurrence.NONE : existing.getRecurrence());
        recurrence.setBackground(FIELD);

        JTextArea subtasks = new JTextArea(existing == null ? "" : existing.getSubtasksAsText(), 3, 20);
        subtasks.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.PLAIN, 13));
        subtasks.setBackground(FIELD);
        subtasks.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane subtasksScroll = new JScrollPane(subtasks);
        subtasksScroll.setPreferredSize(new Dimension(410, 80));
        subtasksScroll.getViewport().setBackground(FIELD);
        subtasksScroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 180), 1, true));

        category.setBackground(FIELD);
        priority.setBackground(FIELD);
        status.setBackground(FIELD);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0); c.anchor = GridBagConstraints.WEST;
        addField(fields, c, "Task title", title);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setPreferredSize(new Dimension(410, 95));
        descriptionScroll.setOpaque(false);
        descriptionScroll.getViewport().setBackground(FIELD);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 180), 1, true));
        addField(fields, c, "Description", descriptionScroll);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Sub-tasks / Checklist (one item per line)", subtasksScroll);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Category", category);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Priority", priority);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Status", status);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Due date (optional, YYYY-MM-DD)", dueDate);
        c.gridy++; c.insets = new Insets(9, 0, 5, 0);
        addField(fields, c, "Repeat", recurrence);
        root.add(fields, BorderLayout.CENTER);

        final Draft[] result = new Draft[1];
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton cancel = new SoftButton("Cancel", SoftButton.Tone.QUIET);
        JButton save = new SoftButton(existing == null ? "Create task" : "Save changes", SoftButton.Tone.ACTION);
        cancel.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        save.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        cancel.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.PLAIN, 13));
        save.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.BOLD, 13));
        cancel.addActionListener(event -> dialog.dispose());
        save.addActionListener(event -> {
            String cleanTitle = title.getText().trim();
            if (cleanTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a task title.", "Title required", JOptionPane.WARNING_MESSAGE);
                title.requestFocusInWindow();
                return;
            }
            Object rawCategory = category.getEditor().getItem();
            String categoryName = rawCategory == null ? "" : rawCategory.toString().trim();
            if (categoryName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter or select a category.", "Category required", JOptionPane.WARNING_MESSAGE);
                category.requestFocusInWindow();
                return;
            }
            LocalDate parsedDate = null;
            String rawDate = dueDate.getText().trim();
            if (!rawDate.isEmpty()) {
                try {
                    parsedDate = LocalDate.parse(rawDate);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Use a real date in YYYY-MM-DD format, for example 2026-11-30.",
                            "Check the due date", JOptionPane.WARNING_MESSAGE);
                    dueDate.requestFocusInWindow();
                    return;
                }
            }

            List<Subtask> parsedSubtasks = new ArrayList<>();
            String[] lines = subtasks.getText().split("\\R");
            for (String line : lines) {
                String cleanLine = line.trim();
                if (!cleanLine.isEmpty()) {
                    boolean wasDone = false;
                    if (existing != null) {
                        for (Subtask oldSub : existing.getSubtasks()) {
                            if (oldSub.getTitle().equalsIgnoreCase(cleanLine)) {
                                wasDone = oldSub.isCompleted();
                                break;
                            }
                        }
                    }
                    parsedSubtasks.add(new Subtask(cleanLine, wasDone));
                }
            }

            result[0] = new Draft(cleanTitle, description.getText().trim(), categoryName,
                    (Priority) priority.getSelectedItem(), (TaskStatus) status.getSelectedItem(), parsedDate,
                    (Recurrence) recurrence.getSelectedItem(), parsedSubtasks);
            dialog.dispose();
        });
        actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static void addField(JPanel panel, GridBagConstraints c, String label, Component component) {
        JLabel caption = new JLabel(label);
        caption.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.BOLD, 13));
        caption.setForeground(INK);
        panel.add(caption, c);
        GridBagConstraints input = (GridBagConstraints) c.clone();
        input.gridy = c.gridy + 1;
        input.insets = new Insets(0, 0, 4, 0);
        panel.add(component, input);
        c.gridy++;
    }

    private static void styleInput(JTextField field) {
        field.setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.PLAIN, 13));
        field.setBackground(FIELD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 190), 1, true),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    /** Validated values returned by the editor.
     * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
     */
    static final class Draft {
        final String title;
        final String description;
        final String category;
        final Priority priority;
        final TaskStatus status;
        final LocalDate dueDate;
        final Recurrence recurrence;
        final List<Subtask> subtasks;

        Draft(String title, String description, String category, Priority priority,
              TaskStatus status, LocalDate dueDate, Recurrence recurrence, List<Subtask> subtasks) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = priority;
            this.status = status;
            this.dueDate = dueDate;
            this.recurrence = recurrence == null ? Recurrence.NONE : recurrence;
            this.subtasks = subtasks == null ? new ArrayList<>() : subtasks;
        }
    }
}
