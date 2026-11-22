package edu.oel.tasknest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Main window and task interaction controller.
 * @author OEL Student Team (add team names before submission)
 */
final class TaskManagerPanel {
    private static final Color CARD = new Color(255, 248, 232, 190);
    private static final Color INK = new Color(37, 57, 86);
    private static final Color MUTED = new Color(65, 84, 112);
    private static final Color BLUE = new Color(48, 98, 160);
    private static final Color BLUE_LIGHT = new Color(224, 235, 249);
    private static final Color BORDER = new Color(255, 255, 255, 160);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final TaskRepository repository = TaskRepository.defaultRepository();
    private final List<Task> tasks = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final JPanel sidebar = new JPanel();
    private final JPanel taskList = new JPanel();
    private final JLabel pageTitle = new JLabel("All tasks");
    private final JLabel summary = new JLabel();
    private final SearchField search = new SearchField();
    private final JFrame frame = new JFrame("TaskNest | A little more organized");
    private String activeFilter = "All tasks";
    private String activeCategory;
    private boolean loadFailed;
    private boolean maximized;
    private Rectangle restoreBounds;

    TaskManagerPanel() {
        loadData();
    }

    /** Builds and displays the application window.
     */
    void showWindow() {
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle workArea = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(1080, Math.max(780, workArea.width - 24));
        int height = Math.min(720, Math.max(450, workArea.height - 20));
        frame.setMinimumSize(new Dimension(Math.min(820, width), Math.min(500, height)));
        frame.setSize(width, height);
        frame.setLocation(workArea.x + Math.max(0, (workArea.width - width) / 2),
                workArea.y + Math.max(0, (workArea.height - height) / 2));
        frame.setContentPane(buildRoot());
        refresh();
        frame.setVisible(true);
        if (loadFailed) {
            JOptionPane.showMessageDialog(frame,
                    "TaskNest could not read its saved task file. Your existing file has been left untouched.\n\n"
                            + "Data file: " + repositoryPath() + "\n\n"
                            + "The app is open in read-only mode until the saved file is repaired.",
                    "Saved data needs attention", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildRoot() {
        JPanel root = new ColorWashPanel(new BorderLayout());
        root.add(buildTitleBar(), BorderLayout.NORTH);
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setOpaque(false);
        JScrollPane sideScroll = new JScrollPane(buildSidebar());
        sideScroll.setPreferredSize(new Dimension(252, 600));
        sideScroll.setBorder(BorderFactory.createEmptyBorder());
        sideScroll.setOpaque(false);
        sideScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.getVerticalScrollBar().setUnitIncrement(18);
        sideScroll.getVerticalScrollBar().setOpaque(false);
        sideScroll.getViewport().setOpaque(false);
        workspace.add(sideScroll, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 34, 26, 34));
        content.add(buildHeader(), BorderLayout.NORTH);

        taskList.setOpaque(false);
        taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        content.add(scroll, BorderLayout.CENTER);
        workspace.add(content, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(83, 112, 163));
        bar.setPreferredSize(new Dimension(0, 38));
        bar.setBorder(new EmptyBorder(0, 15, 0, 0));

        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        identity.setOpaque(false);
        JLabel monogram = new JLabel("T", SwingConstants.CENTER);
        monogram.setForeground(new Color(255, 247, 231));
        monogram.setFont(new Font("Georgia", Font.BOLD, 14));
        identity.add(monogram);
        JLabel title = new JLabel("TaskNest  |  A little more organized");
        title.setForeground(new Color(255, 247, 231));
        title.setFont(new Font("Palatino Linotype", Font.BOLD, 13));
        identity.add(title);
        bar.add(identity, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);
        WindowControlButton minimize = new WindowControlButton(WindowControl.MINIMIZE);
        minimize.addActionListener(event -> frame.setState(Frame.ICONIFIED));
        WindowControlButton maximize = new WindowControlButton(WindowControl.MAXIMIZE);
        maximize.addActionListener(event -> toggleMaximize());
        WindowControlButton close = new WindowControlButton(WindowControl.CLOSE);
        close.addActionListener(event -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        controls.add(minimize);
        controls.add(maximize);
        controls.add(close);
        bar.add(controls, BorderLayout.EAST);

        MouseAdapter dragHandler = new MouseAdapter() {
            private java.awt.Point pressLocation;
            private java.awt.Point windowLocation;

            @Override public void mousePressed(MouseEvent event) {
                if (maximized || event.getButton() != MouseEvent.BUTTON1) return;
                pressLocation = event.getLocationOnScreen();
                windowLocation = frame.getLocation();
            }

            @Override public void mouseDragged(MouseEvent event) {
                if (pressLocation == null || windowLocation == null) return;
                java.awt.Point current = event.getLocationOnScreen();
                frame.setLocation(windowLocation.x + current.x - pressLocation.x,
                        windowLocation.y + current.y - pressLocation.y);
            }

            @Override public void mouseReleased(MouseEvent event) {
                pressLocation = null;
                windowLocation = null;
            }

            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) toggleMaximize();
            }
        };
        bar.addMouseListener(dragHandler);
        bar.addMouseMotionListener(dragHandler);
        identity.addMouseListener(dragHandler);
        identity.addMouseMotionListener(dragHandler);
        title.addMouseListener(dragHandler);
        title.addMouseMotionListener(dragHandler);
        monogram.addMouseListener(dragHandler);
        monogram.addMouseMotionListener(dragHandler);
        return bar;
    }

    private void toggleMaximize() {
        if (!maximized) {
            restoreBounds = frame.getBounds();
            frame.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            maximized = true;
        } else {
            if (restoreBounds != null) frame.setBounds(restoreBounds);
            maximized = false;
        }
    }

    private JPanel buildSidebar() {
        sidebar.setOpaque(false);
        sidebar.setBorder(new EmptyBorder(20, 18, 16, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        return sidebar;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 18));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("Georgia", Font.BOLD, 29));
        pageTitle.setForeground(INK);
        JLabel tagline = new JLabel("Make room for what matters.");
        tagline.setFont(new Font("Palatino Linotype", Font.PLAIN, 15));
        tagline.setForeground(MUTED);
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(5));
        titles.add(tagline);
        titleRow.add(titles, BorderLayout.WEST);
        JButton add = actionButton("+  New task", true);
        add.addActionListener(event -> createTask());
        titleRow.add(add, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        search.setText("Search your tasks");
        search.setForeground(new Color(146, 153, 163));
        search.setBorder(new EmptyBorder(11, 15, 11, 15));
        search.setBackground(new Color(255, 248, 233, 175));
        search.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                if ("Search your tasks".equals(search.getText())) {
                    search.setText(""); search.setForeground(INK);
                }
            }
            @Override public void focusLost(FocusEvent event) {
                if (search.getText().isBlank()) {
                    search.setText("Search your tasks"); search.setForeground(new Color(146, 153, 163));
                }
            }
        });
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() { refreshTasks(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { changed(); }
        });
        summary.setForeground(MUTED);
        summary.setFont(new Font("Palatino Linotype", Font.PLAIN, 13));
        searchRow.add(search, BorderLayout.CENTER);
        searchRow.add(summary, BorderLayout.EAST);
        header.add(searchRow, BorderLayout.SOUTH);
        return header;
    }

    private void refresh() {
        refreshSidebar();
        refreshTasks();
    }

    private void refreshSidebar() {
        sidebar.removeAll();
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JPanel mark = new RoundedPanel(new Color(255, 248, 232, 205), 22);
        mark.setPreferredSize(new Dimension(40, 40));
        mark.setLayout(new BorderLayout());
        JLabel check = new JLabel("T", SwingConstants.CENTER);
        check.setForeground(BLUE);
        check.setFont(new Font("Georgia", Font.BOLD, 20));
        mark.add(check);
        JLabel name = new JLabel("  TaskNest");
        name.setForeground(INK);
        name.setFont(new Font("Georgia", Font.BOLD, 20));
        brand.add(mark);
        brand.add(name);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(26));
        addSectionLabel("YOUR SPACE");
        addNavItem("All tasks", null);
        addNavItem("Pending", null);
        addNavItem("In Progress", null);
        addNavItem("Completed", null);
        addNavItem("High Priority", null);
        sidebar.add(Box.createVerticalStrut(16));
        JPanel categoryHeader = new JPanel(new BorderLayout());
        categoryHeader.setOpaque(false);
        JLabel categoryLabel = sectionLabel("CATEGORIES");
        SoftButton addCategory = new SoftButton("+", SoftButton.Tone.QUIET);
        addCategory.setForeground(BLUE);
        addCategory.setFont(new Font("Georgia", Font.BOLD, 18));
        addCategory.setBorder(new EmptyBorder(1, 9, 1, 9));
        addCategory.setToolTipText("Add a category");
        addCategory.setEnabled(!loadFailed);
        addCategory.addActionListener(event -> createCategory());
        categoryHeader.add(categoryLabel, BorderLayout.WEST);
        categoryHeader.add(addCategory, BorderLayout.EAST);
        categoryHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryHeader.setPreferredSize(new Dimension(198, 32));
        categoryHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        sidebar.add(categoryHeader);
        for (Category category : categories) addNavItem(category.getName(), category.getName());
        sidebar.add(Box.createVerticalGlue());
        JLabel storage = new JLabel("Saved on this device");
        storage.setFont(storage.getFont().deriveFont(Font.PLAIN, 10f));
        storage.setForeground(MUTED);
        storage.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(storage);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addSectionLabel(String text) {
        JLabel label = sectionLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(label);
        sidebar.add(Box.createVerticalStrut(9));
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Palatino Linotype", Font.BOLD, 10));
        return label;
    }

    private void addNavItem(String label, String categoryName) {
        boolean selected = categoryName == null ? activeCategory == null && activeFilter.equals(label)
                : categoryName.equalsIgnoreCase(activeCategory == null ? "" : activeCategory);
        SoftButton button = new SoftButton(label, SoftButton.Tone.QUIET);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        button.setPreferredSize(new Dimension(198, 35));
        button.setBorder(new EmptyBorder(8, 14, 8, 12));
        button.setFont(new Font("Palatino Linotype", selected ? Font.BOLD : Font.PLAIN, 14));
        button.setForeground(selected ? BLUE : INK);
        button.setSelectedStyle(selected);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(event -> {
            if (categoryName != null) {
                activeCategory = categoryName;
                activeFilter = "All tasks";
            } else {
                activeCategory = null;
                activeFilter = label;
            }
            refresh();
        });
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private void refreshTasks() {
        if (taskList == null) return;
        taskList.removeAll();
        List<Task> visible = tasks.stream().filter(this::matchesFilter)
                .filter(this::matchesSearch)
                .sorted(Comparator.comparingInt((Task task) -> task.getPriority().getRank()).reversed()
                        .thenComparing(task -> task.getDueDate() == null ? LocalDate.MAX : task.getDueDate())
                        .thenComparing(Task::getCreatedAt))
                .collect(Collectors.toList());
        long openCount = tasks.stream().filter(task -> task.getStatus() != TaskStatus.COMPLETED).count();
        long completedCount = tasks.size() - openCount;
        summary.setText(openCount + " open  ·  " + completedCount + " completed");
        pageTitle.setText(activeCategory == null ? activeFilter : activeCategory);
        if (visible.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(64, 12, 0, 0));
            JLabel title = new JLabel(tasks.isEmpty() ? "Your list is ready." : "Nothing to show here.");
            title.setForeground(INK);
            title.setFont(new Font("Georgia", Font.BOLD, 22));
            JLabel hint = new JLabel(tasks.isEmpty() ? "Add a task and give your day a little shape." : "Try a different search or filter.");
            hint.setForeground(MUTED);
            hint.setFont(new Font("Palatino Linotype", Font.PLAIN, 15));
            JButton add = actionButton("+  Create your first task", true);
            add.addActionListener(event -> createTask());
            empty.add(title);
            empty.add(Box.createVerticalStrut(8));
            empty.add(hint);
            if (tasks.isEmpty() && !loadFailed) {
                empty.add(Box.createVerticalStrut(20));
                empty.add(add);
            }
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            taskList.add(empty);
        } else {
            for (Task task : visible) {
                taskList.add(new TaskCard(task));
                taskList.add(Box.createVerticalStrut(12));
            }
        }
        taskList.revalidate();
        taskList.repaint();
    }

    private boolean matchesFilter(Task task) {
        if (activeCategory != null && !task.getCategory().getName().equalsIgnoreCase(activeCategory)) return false;
        switch (activeFilter) {
            case "Pending": return task.getStatus() == TaskStatus.PENDING;
            case "In Progress": return task.getStatus() == TaskStatus.IN_PROGRESS;
            case "Completed": return task.getStatus() == TaskStatus.COMPLETED;
            case "High Priority": return task.getPriority() == Priority.HIGH;
            default: return true;
        }
    }

    private boolean matchesSearch(Task task) {
        String query = search.getText().trim();
        if (query.isEmpty() || "Search your tasks".equals(query)) return true;
        String text = (task.getTitle() + " " + task.getDescription() + " " + task.getCategory().getName())
                .toLowerCase(Locale.ROOT);
        return text.contains(query.toLowerCase(Locale.ROOT));
    }

    private void createTask() {
        if (loadFailed) return;
        TaskEditorDialog.Draft draft = TaskEditorDialog.show(frame, null, categories);
        if (draft == null) return;
        Category category = ensureCategory(draft.category);
        tasks.add(new Task(draft.title, draft.description, category, draft.priority, draft.status, draft.dueDate));
        saveAndRefresh();
    }

    private void editTask(Task task) {
        if (loadFailed) return;
        TaskEditorDialog.Draft draft = TaskEditorDialog.show(frame, task, categories);
        if (draft == null) return;
        task.update(draft.title, draft.description, ensureCategory(draft.category),
                draft.priority, draft.status, draft.dueDate);
        saveAndRefresh();
    }

    private void deleteTask(Task task) {
        if (loadFailed) return;
        int answer = JOptionPane.showConfirmDialog(frame, "Remove “" + task.getTitle() + "” from your list?",
                "Delete task", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            tasks.remove(task);
            saveAndRefresh();
        }
    }

    private void setCompleted(Task task, boolean completed) {
        if (loadFailed) return;
        TaskStatus next = completed ? TaskStatus.COMPLETED : TaskStatus.PENDING;
        task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), next, task.getDueDate());
        saveAndRefresh();
    }

    private void createCategory() {
        if (loadFailed) return;
        String value = JOptionPane.showInputDialog(frame, "Name your category", "New category", JOptionPane.PLAIN_MESSAGE);
        if (value == null) return;
        String clean = value.trim();
        if (clean.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "A category needs a name.", "Category required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (categories.stream().anyMatch(category -> category.getName().equalsIgnoreCase(clean))) {
            JOptionPane.showMessageDialog(frame, "That category already exists.", "Category already exists", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        categories.add(new Category(clean));
        saveAndRefresh();
    }

    private Category ensureCategory(String name) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name.trim())) return category;
        }
        Category category = new Category(name);
        categories.add(category);
        return category;
    }

    private void saveAndRefresh() {
        try {
            repository.save(tasks, categories);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Your change could not be saved.\n\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    private void loadData() {
        try {
            TaskRepository.AppData data = repository.load();
            tasks.addAll(data.getTasks());
            categories.addAll(data.getCategories());
        } catch (IOException | RuntimeException ex) {
            loadFailed = true;
        }
    }

    private String repositoryPath() {
        return Path.of(System.getProperty("user.home"), ".tasknest", "tasks.properties").toString();
    }

    private JButton actionButton(String text, boolean primary) {
        SoftButton button = new SoftButton(text, primary ? SoftButton.Tone.ACTION : SoftButton.Tone.QUIET);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setFont(new Font("Palatino Linotype", Font.BOLD, 14));
        button.setEnabled(!loadFailed);
        return button;
    }

    private final class TaskCard extends RoundedPanel {
        private static final long serialVersionUID = 1L;

        TaskCard(Task task) {
            super(CARD, 18);
            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(15, 17, 15, 14));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

            JCheckBox done = new JCheckBox();
            done.setSelected(task.getStatus() == TaskStatus.COMPLETED);
            done.setOpaque(false);
            done.setFocusPainted(false);
            done.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            done.setToolTipText("Mark task complete");
            done.setEnabled(!loadFailed);
            done.addActionListener(event -> setCompleted(task, done.isSelected()));
            add(done, BorderLayout.WEST);

            JPanel details = new JPanel();
            details.setOpaque(false);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(task.getTitle());
            title.setForeground(task.getStatus() == TaskStatus.COMPLETED ? MUTED : INK);
            title.setFont(new Font("Palatino Linotype", Font.BOLD, 16));
            if (task.getStatus() == TaskStatus.COMPLETED) {
                title.setText("<html><span style='text-decoration:line-through'>" + escapeHtml(task.getTitle()) + "</span></html>");
            } else if (task.getTitle().startsWith("<html>")) {
                title.setText("<html>" + escapeHtml(task.getTitle()) + "</html>");
            }
            String descriptionText = task.getDescription().isBlank() ? "No description" : ellipsize(task.getDescription(), 88);
            JLabel description = new JLabel(descriptionText.startsWith("<html>")
                    ? "<html>" + escapeHtml(descriptionText) + "</html>" : descriptionText);
            description.setForeground(MUTED);
            description.setFont(description.getFont().deriveFont(Font.PLAIN, 12f));
            JPanel metadata = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
            metadata.setOpaque(false);
            metadata.add(pill(task.getCategory().getName(), new Color(239, 232, 216), INK));
            metadata.add(pill(task.getPriority().toString(), priorityBg(task.getPriority()), priorityFg(task.getPriority())));
            metadata.add(pill(task.getStatus().toString(), statusBg(task.getStatus()), statusFg(task.getStatus())));
            if (task.getDueDate() != null) {
                Color dueColor = task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED
                        ? new Color(179, 79, 62) : MUTED;
                metadata.add(new JLabel("Due " + task.getDueDate().format(DATE_FORMAT)) {{
                    setForeground(dueColor); setFont(getFont().deriveFont(Font.PLAIN, 11f));
                }});
            }
            details.add(title);
            details.add(Box.createVerticalStrut(5));
            details.add(description);
            details.add(Box.createVerticalStrut(9));
            details.add(metadata);
            add(details, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actions.setOpaque(false);
            JButton edit = smallButton("Edit");
            edit.addActionListener(event -> editTask(task));
            JButton delete = smallButton("Delete");
            delete.setForeground(new Color(154, 76, 65));
            delete.addActionListener(event -> deleteTask(task));
            actions.add(edit);
            actions.add(delete);
            add(actions, BorderLayout.EAST);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2) editTask(task);
                }
            });
        }
    }

    private JLabel pill(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setFont(new Font("Palatino Linotype", Font.BOLD, 10));
        label.setBorder(new EmptyBorder(4, 8, 4, 8));
        return label;
    }

    private JButton smallButton(String text) {
        SoftButton button = new SoftButton(text, "Delete".equals(text)
                ? SoftButton.Tone.DESTRUCTIVE : SoftButton.Tone.QUIET);
        button.setFont(new Font("Palatino Linotype", Font.PLAIN, 12));
        button.setBorder(new EmptyBorder(6, 9, 6, 9));
        button.setEnabled(!loadFailed);
        return button;
    }

    private Color priorityBg(Priority priority) {
        switch (priority) {
            case HIGH: return new Color(249, 226, 216);
            case MEDIUM: return new Color(250, 239, 211);
            default: return new Color(232, 238, 230);
        }
    }

    private Color priorityFg(Priority priority) {
        switch (priority) {
            case HIGH: return new Color(151, 74, 55);
            case MEDIUM: return new Color(139, 99, 35);
            default: return new Color(72, 113, 81);
        }
    }

    private Color statusBg(TaskStatus status) {
        switch (status) {
            case COMPLETED: return new Color(226, 239, 226);
            case IN_PROGRESS: return BLUE_LIGHT;
            default: return new Color(237, 237, 235);
        }
    }

    private Color statusFg(TaskStatus status) {
        switch (status) {
            case COMPLETED: return new Color(63, 111, 73);
            case IN_PROGRESS: return BLUE;
            default: return new Color(94, 104, 113);
        }
    }

    private String ellipsize(String text, int max) {
        String oneLine = text.replaceAll("\\s+", " ");
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max - 1) + "…";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private enum WindowControl { MINIMIZE, MAXIMIZE, CLOSE }

    private final class WindowControlButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final WindowControl control;

        WindowControlButton(WindowControl control) {
            this.control = control;
            setPreferredSize(new Dimension(46, 38));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setToolTipText(control == WindowControl.MINIMIZE ? "Minimize window"
                    : control == WindowControl.MAXIMIZE ? "Maximize or restore window" : "Close window");
            getAccessibleContext().setAccessibleName(getToolTipText());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g.setColor(control == WindowControl.CLOSE ? new Color(181, 80, 77) : new Color(255, 255, 255, 35));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(new Color(255, 247, 231));
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            if (control == WindowControl.MINIMIZE) {
                g.drawLine(centerX - 5, centerY + 4, centerX + 5, centerY + 4);
            } else if (control == WindowControl.MAXIMIZE) {
                g.drawRect(centerX - 5, centerY - 5, 10, 10);
            } else {
                g.drawLine(centerX - 5, centerY - 5, centerX + 5, centerY + 5);
                g.drawLine(centerX + 5, centerY - 5, centerX - 5, centerY + 5);
            }
            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color fill;
        private final int radius;

        RoundedPanel(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class SearchField extends JTextField {
        private static final long serialVersionUID = 1L;

        SearchField() {
            setOpaque(false);
            setFont(new Font("Palatino Linotype", Font.PLAIN, 14));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(new Color(255, 255, 255, 156));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
