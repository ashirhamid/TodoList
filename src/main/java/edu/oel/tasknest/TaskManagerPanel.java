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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Main window and task interaction controller.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
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
    private final ProductivityPanel productivityBar = new ProductivityPanel();
    private final Deque<TaskCommand> undoStack = new ArrayDeque<>();
    private final Deque<TaskCommand> redoStack = new ArrayDeque<>();
    private final JPanel toast = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    private final JLabel toastLabel = new JLabel();
    private final JButton toastUndoButton = new JButton("Undo");
    private javax.swing.Timer toastTimer;
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

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 34, 26, 34));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(buildHeader());
        topSection.add(Box.createVerticalStrut(14));
        topSection.add(productivityBar);
        content.add(topSection, BorderLayout.NORTH);

        taskList.setOpaque(false);
        taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        content.add(scroll, BorderLayout.CENTER);

        toast.setOpaque(true);
        toast.setBackground(new Color(24, 32, 47, 240));
        toast.setBorder(new EmptyBorder(8, 16, 8, 16));
        toast.setVisible(false);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Palatino Linotype", Font.PLAIN, 13));
        toastUndoButton.setFont(new Font("Palatino Linotype", Font.BOLD, 12));
        toastUndoButton.setForeground(new Color(255, 241, 219));
        toastUndoButton.setOpaque(false);
        toastUndoButton.setContentAreaFilled(false);
        toastUndoButton.setFocusPainted(false);
        toastUndoButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 241, 219)));
        toastUndoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toastUndoButton.addActionListener(e -> undo());
        toast.add(toastLabel);
        toast.add(toastUndoButton);
        content.add(toast, BorderLayout.SOUTH);

        workspace.add(content, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undoAction");
        root.getActionMap().put("undoAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { undo(); }
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redoAction");
        root.getActionMap().put("redoAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { redo(); }
        });
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
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        JButton export = actionButton("Export", false);
        export.setToolTipText("Export tasks to Markdown or CSV");
        export.addActionListener(event -> exportTasks());
        JButton add = actionButton("+  New task", true);
        add.addActionListener(event -> createTask());
        headerActions.add(export);
        headerActions.add(add);
        titleRow.add(headerActions, BorderLayout.EAST);
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
        addNavItem("Due Today", null);
        addNavItem("Overdue", null);
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
        productivityBar.update(tasks);
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
        LocalDate today = LocalDate.now();
        switch (activeFilter) {
            case "Pending": return task.getStatus() == TaskStatus.PENDING;
            case "In Progress": return task.getStatus() == TaskStatus.IN_PROGRESS;
            case "Completed": return task.getStatus() == TaskStatus.COMPLETED;
            case "High Priority": return task.getPriority() == Priority.HIGH;
            case "Due Today":
                return task.getStatus() != TaskStatus.COMPLETED &&
                        task.getDueDate() != null && task.getDueDate().isEqual(today);
            case "Overdue":
                return task.getStatus() != TaskStatus.COMPLETED &&
                        task.getDueDate() != null && task.getDueDate().isBefore(today);
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
        Task newTask = new Task(draft.title, draft.description, category, draft.priority, draft.status, draft.dueDate);
        newTask.setRecurrence(draft.recurrence);
        newTask.setSubtasks(draft.subtasks);
        executeCommand(new AddTaskCommand(newTask));
    }

    private void editTask(Task task) {
        if (loadFailed) return;
        TaskEditorDialog.Draft draft = TaskEditorDialog.show(frame, task, categories);
        if (draft == null) return;
        TaskMemento before = new TaskMemento(task);
        Category newCategory = ensureCategory(draft.category);
        TaskMemento after = new TaskMemento(draft.title, draft.description, newCategory,
                draft.priority, draft.status, draft.dueDate, draft.recurrence, draft.subtasks);
        executeCommand(new UpdateTaskCommand(task, before, after));
    }

    private void deleteTask(Task task) {
        if (loadFailed) return;
        int answer = JOptionPane.showConfirmDialog(frame, "Remove “" + task.getTitle() + "” from your list?",
                "Delete task", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            executeCommand(new DeleteTaskCommand(task, tasks.indexOf(task)));
        }
    }

    private void setCompleted(Task task, boolean completed) {
        if (loadFailed) return;
        TaskStatus next = completed ? TaskStatus.COMPLETED : TaskStatus.PENDING;
        executeCommand(new ToggleTaskCommand(task, next));

        if (completed) {
            checkRecurringOnComplete(task);
        }
    }

    private void checkRecurringOnComplete(Task task) {
        if (task.getRecurrence() != Recurrence.NONE && task.getDueDate() != null) {
            LocalDate nextDue = task.getRecurrence().nextDate(task.getDueDate());
            Task nextTask = new Task(task.getTitle(), task.getDescription(), task.getCategory(),
                    task.getPriority(), TaskStatus.PENDING, nextDue);
            nextTask.setRecurrence(task.getRecurrence());
            List<Subtask> resetSubs = new ArrayList<>();
            for (Subtask s : task.getSubtasks()) {
                resetSubs.add(new Subtask(s.getTitle(), false));
            }
            nextTask.setSubtasks(resetSubs);
            tasks.add(nextTask);
            saveAndRefresh();
            showToast("Recurring task scheduled for " + nextDue.format(DATE_FORMAT), false);
        }
    }

    private void toggleSubtask(Task task, Subtask subtask, boolean completed) {
        if (loadFailed) return;
        executeCommand(new ToggleSubtaskCommand(task, subtask, completed));
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
            setMaximumSize(new Dimension(Integer.MAX_VALUE, task.getSubtasks().isEmpty() ? 128 : (128 + task.getSubtasks().size() * 26)));

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
            if (task.getRecurrence() != Recurrence.NONE) {
                metadata.add(pill("🔄 " + task.getRecurrence().getLabel(), new Color(228, 238, 252), BLUE));
            }
            if (!task.getSubtasks().isEmpty()) {
                long doneCount = task.getCompletedSubtaskCount();
                int totalCount = task.getSubtasks().size();
                metadata.add(pill("✓ " + doneCount + "/" + totalCount + " subtasks",
                        doneCount == totalCount ? new Color(220, 252, 231) : new Color(241, 245, 249),
                        doneCount == totalCount ? new Color(21, 128, 61) : INK));
            }
            if (task.getDueDate() != null) {
                LocalDate today = LocalDate.now();
                LocalDate due = task.getDueDate();
                if (task.getStatus() != TaskStatus.COMPLETED) {
                    if (due.isBefore(today)) {
                        metadata.add(pill("🔴 Overdue", new Color(254, 226, 226), new Color(185, 28, 28)));
                    } else if (due.isEqual(today)) {
                        metadata.add(pill("🟡 Due Today", new Color(254, 243, 199), new Color(180, 83, 9)));
                    } else if (!due.isAfter(today.plusDays(3))) {
                        metadata.add(pill("🟢 Upcoming", new Color(220, 252, 231), new Color(21, 128, 61)));
                    }
                }
                Color dueColor = due.isBefore(today) && task.getStatus() != TaskStatus.COMPLETED
                        ? new Color(179, 79, 62) : MUTED;
                metadata.add(new JLabel("Due " + due.format(DATE_FORMAT)) {{
                    setForeground(dueColor); setFont(getFont().deriveFont(Font.PLAIN, 11f));
                }});
            }
            details.add(title);
            details.add(Box.createVerticalStrut(5));
            details.add(description);
            details.add(Box.createVerticalStrut(9));
            details.add(metadata);
            if (!task.getSubtasks().isEmpty()) {
                JPanel subList = new JPanel();
                subList.setOpaque(false);
                subList.setLayout(new BoxLayout(subList, BoxLayout.Y_AXIS));
                subList.setBorder(new EmptyBorder(6, 4, 2, 4));
                for (Subtask sub : task.getSubtasks()) {
                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                    row.setOpaque(false);
                    JCheckBox sc = new JCheckBox();
                    sc.setSelected(sub.isCompleted());
                    sc.setOpaque(false);
                    sc.setFocusPainted(false);
                    sc.setEnabled(!loadFailed);
                    sc.addActionListener(e -> toggleSubtask(task, sub, sc.isSelected()));
                    JLabel st = new JLabel(sub.isCompleted()
                            ? "<html><span style='text-decoration:line-through;color:#999999'>" + escapeHtml(sub.getTitle()) + "</span></html>"
                            : sub.getTitle());
                    st.setFont(new Font("Palatino Linotype", Font.PLAIN, 12));
                    st.setForeground(INK);
                    row.add(sc);
                    row.add(st);
                    subList.add(row);
                }
                details.add(Box.createVerticalStrut(4));
                details.add(subList);
            }
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

    private void exportTasks() {
        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No tasks available to export.", "Export Tasks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] options = {"Markdown Checklist (.md)", "Spreadsheet (.csv)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame,
                "Select the export format for your " + tasks.size() + " tasks:",
                "Export Tasks",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            exportToMarkdown();
        } else if (choice == 1) {
            exportToCsv();
        }
    }

    private void exportToMarkdown() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Tasks as Markdown");
        chooser.setSelectedFile(new File("TaskNest-Tasks-" + LocalDate.now() + ".md"));
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown files (*.md)", "md"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
            file = new File(file.getParentFile(), file.getName() + ".md");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("# TaskNest Tasks Export\n\n");
            writer.write("*Exported on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "*\n\n");
            long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            writer.write(String.format(Locale.ROOT, "**Summary:** %d total | %d completed | %d pending\n\n",
                    tasks.size(), completed, tasks.size() - completed));
            writer.write("---\n\n");

            for (Task task : tasks) {
                String check = task.getStatus() == TaskStatus.COMPLETED ? "[x]" : "[ ]";
                writer.write(String.format(Locale.ROOT, "- %s **%s** `[%s]` `[%s]`",
                        check, task.getTitle(), task.getCategory().getName(), task.getPriority()));
                if (task.getDueDate() != null) {
                    writer.write(" (Due: " + task.getDueDate() + ")");
                }
                writer.write("\n");
                if (!task.getDescription().isBlank()) {
                    writer.write("  > " + task.getDescription().replace("\n", " ") + "\n");
                }
            }
            JOptionPane.showMessageDialog(frame, "Tasks successfully exported to:\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to export tasks:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Tasks as CSV");
        chooser.setSelectedFile(new File("TaskNest-Tasks-" + LocalDate.now() + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("Title,Description,Category,Priority,Status,DueDate,CreatedAt\n");
            for (Task task : tasks) {
                writer.write(String.format(Locale.ROOT, "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCsv(task.getTitle()),
                        escapeCsv(task.getDescription()),
                        escapeCsv(task.getCategory().getName()),
                        task.getPriority().name(),
                        task.getStatus().name(),
                        task.getDueDate() == null ? "" : task.getDueDate().toString(),
                        task.getCreatedAt() == null ? "" : task.getCreatedAt().toString()));
            }
            JOptionPane.showMessageDialog(frame, "Tasks successfully exported to:\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to export tasks:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
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

    private static final class ProductivityPanel extends RoundedPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel percentLabel = new JLabel("0% completed");
        private final JLabel statsLabel = new JLabel("0 of 0 tasks");
        private final JLabel urgencyStats = new JLabel("");
        private double progress = 0.0;

        ProductivityPanel() {
            super(CARD, 16);
            setLayout(new BorderLayout(0, 8));
            setBorder(new EmptyBorder(12, 18, 12, 18));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            JLabel title = new JLabel("Productivity");
            title.setFont(new Font("Georgia", Font.BOLD, 14));
            title.setForeground(INK);
            percentLabel.setFont(new Font("Palatino Linotype", Font.BOLD, 13));
            percentLabel.setForeground(BLUE);
            statsLabel.setFont(new Font("Palatino Linotype", Font.PLAIN, 12));
            statsLabel.setForeground(MUTED);
            left.add(title);
            left.add(percentLabel);
            JLabel dot = new JLabel("·");
            dot.setForeground(MUTED);
            left.add(dot);
            left.add(statsLabel);
            topRow.add(left, BorderLayout.WEST);

            urgencyStats.setFont(new Font("Palatino Linotype", Font.BOLD, 12));
            urgencyStats.setForeground(MUTED);
            topRow.add(urgencyStats, BorderLayout.EAST);

            add(topRow, BorderLayout.NORTH);

            JPanel barTrack = new JPanel() {
                private static final long serialVersionUID = 1L;
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = getHeight();
                    int w = getWidth();
                    g2.setColor(new Color(230, 226, 215));
                    g2.fillRoundRect(0, 0, w, h, h, h);
                    if (progress > 0) {
                        int fillW = Math.max(h, (int) Math.round(w * Math.min(1.0, progress)));
                        g2.setColor(progress >= 1.0 ? new Color(63, 140, 85) : BLUE);
                        g2.fillRoundRect(0, 0, fillW, h, h, h);
                    }
                    g2.dispose();
                }
            };
            barTrack.setOpaque(false);
            barTrack.setPreferredSize(new Dimension(0, 8));
            barTrack.setMinimumSize(new Dimension(0, 8));
            add(barTrack, BorderLayout.CENTER);
        }

        void update(List<Task> allTasks) {
            int total = allTasks.size();
            long completed = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            LocalDate today = LocalDate.now();
            long overdue = allTasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED
                    && t.getDueDate() != null && t.getDueDate().isBefore(today)).count();
            long dueToday = allTasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED
                    && t.getDueDate() != null && t.getDueDate().isEqual(today)).count();

            if (total == 0) {
                progress = 0.0;
                percentLabel.setText("0%");
                statsLabel.setText("No tasks created yet");
                urgencyStats.setText("");
            } else {
                progress = (double) completed / total;
                int pct = (int) Math.round(progress * 100);
                percentLabel.setText(pct + "% completed");
                long totalSubtasks = allTasks.stream().mapToLong(t -> t.getSubtasks().size()).sum();
                long doneSubtasks = allTasks.stream().mapToLong(Task::getCompletedSubtaskCount).sum();
                if (totalSubtasks > 0) {
                    statsLabel.setText(completed + " of " + total + " tasks · " + doneSubtasks + "/" + totalSubtasks + " subtasks");
                } else {
                    statsLabel.setText(completed + " of " + total + " tasks");
                }

                StringBuilder urgency = new StringBuilder();
                if (overdue > 0) urgency.append(overdue).append(" overdue  ");
                if (dueToday > 0) urgency.append(dueToday).append(" due today  ");
                urgencyStats.setText(urgency.toString().trim());
                urgencyStats.setForeground(overdue > 0 ? new Color(179, 79, 62) : new Color(160, 105, 30));
            }
            repaint();
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

    private void executeCommand(TaskCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    private void undo() {
        if (undoStack.isEmpty()) {
            showToast("Nothing to undo", false);
            return;
        }
        TaskCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    private void redo() {
        if (redoStack.isEmpty()) {
            showToast("Nothing to redo", false);
            return;
        }
        TaskCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    private void showToast(String message, boolean showUndo) {
        toastLabel.setText(message);
        toastUndoButton.setVisible(showUndo && !undoStack.isEmpty());
        toast.setVisible(true);
        toast.revalidate();
        toast.repaint();
        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        toastTimer = new javax.swing.Timer(5000, e -> {
            toast.setVisible(false);
            toast.revalidate();
            toast.repaint();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private static final class TaskMemento {
        final String title;
        final String description;
        final Category category;
        final Priority priority;
        final TaskStatus status;
        final LocalDate dueDate;
        final Recurrence recurrence;
        final List<Subtask> subtasks;

        TaskMemento(Task t) {
            this(t.getTitle(), t.getDescription(), t.getCategory(), t.getPriority(),
                 t.getStatus(), t.getDueDate(), t.getRecurrence(), t.getSubtasks());
        }

        TaskMemento(String title, String description, Category category, Priority priority,
                    TaskStatus status, LocalDate dueDate, Recurrence recurrence, List<Subtask> subtasks) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = priority;
            this.status = status;
            this.dueDate = dueDate;
            this.recurrence = recurrence;
            this.subtasks = new ArrayList<>();
            if (subtasks != null) {
                for (Subtask s : subtasks) {
                    this.subtasks.add(new Subtask(s.getTitle(), s.isCompleted()));
                }
            }
        }

        void apply(Task t) {
            t.update(title, description, category, priority, status, dueDate, recurrence, subtasks);
        }
    }

    private final class AddTaskCommand implements TaskCommand {
        private final Task task;
        AddTaskCommand(Task task) { this.task = task; }
        @Override public void execute() {
            if (!tasks.contains(task)) tasks.add(task);
            saveAndRefresh();
            showToast("Added: “" + task.getTitle() + "”", true);
        }
        @Override public void undo() {
            tasks.remove(task);
            saveAndRefresh();
            showToast("Undone: add “" + task.getTitle() + "”", false);
        }
        @Override public String getDescription() { return "Add “" + task.getTitle() + "”"; }
    }

    private final class DeleteTaskCommand implements TaskCommand {
        private final Task task;
        private final int index;
        DeleteTaskCommand(Task task, int index) {
            this.task = task;
            this.index = Math.max(0, index);
        }
        @Override public void execute() {
            tasks.remove(task);
            saveAndRefresh();
            showToast("Deleted: “" + task.getTitle() + "”", true);
        }
        @Override public void undo() {
            tasks.add(Math.min(index, tasks.size()), task);
            saveAndRefresh();
            showToast("Restored: “" + task.getTitle() + "”", false);
        }
        @Override public String getDescription() { return "Delete “" + task.getTitle() + "”"; }
    }

    private final class UpdateTaskCommand implements TaskCommand {
        private final Task task;
        private final TaskMemento before;
        private final TaskMemento after;
        UpdateTaskCommand(Task task, TaskMemento before, TaskMemento after) {
            this.task = task;
            this.before = before;
            this.after = after;
        }
        @Override public void execute() {
            after.apply(task);
            saveAndRefresh();
            showToast("Updated: “" + task.getTitle() + "”", true);
        }
        @Override public void undo() {
            before.apply(task);
            saveAndRefresh();
            showToast("Undone: update “" + task.getTitle() + "”", false);
        }
        @Override public String getDescription() { return "Update “" + task.getTitle() + "”"; }
    }

    private final class ToggleTaskCommand implements TaskCommand {
        private final Task task;
        private final TaskStatus prevStatus;
        private final TaskStatus newStatus;
        private final List<Boolean> prevSubtaskStates = new ArrayList<>();

        ToggleTaskCommand(Task task, TaskStatus newStatus) {
            this.task = task;
            this.prevStatus = task.getStatus();
            this.newStatus = newStatus;
            for (Subtask s : task.getSubtasks()) {
                prevSubtaskStates.add(s.isCompleted());
            }
        }
        @Override public void execute() {
            task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), newStatus, task.getDueDate());
            boolean setSubtasksCompleted = (newStatus == TaskStatus.COMPLETED);
            for (Subtask s : task.getSubtasks()) {
                s.setCompleted(setSubtasksCompleted);
            }
            saveAndRefresh();
            showToast(newStatus == TaskStatus.COMPLETED ? "Completed: “" + task.getTitle() + "”" : "Marked pending: “" + task.getTitle() + "”", true);
        }
        @Override public void undo() {
            task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), prevStatus, task.getDueDate());
            for (int i = 0; i < task.getSubtasks().size() && i < prevSubtaskStates.size(); i++) {
                task.getSubtasks().get(i).setCompleted(prevSubtaskStates.get(i));
            }
            saveAndRefresh();
            showToast("Undone: status change", false);
        }
        @Override public String getDescription() { return "Toggle “" + task.getTitle() + "”"; }
    }

    private final class ToggleSubtaskCommand implements TaskCommand {
        private final Task task;
        private final Subtask subtask;
        private final boolean newState;
        private final TaskStatus prevTaskStatus;
        private TaskStatus nextTaskStatus;

        ToggleSubtaskCommand(Task task, Subtask subtask, boolean newState) {
            this.task = task;
            this.subtask = subtask;
            this.newState = newState;
            this.prevTaskStatus = task.getStatus();
        }
        @Override public void execute() {
            subtask.setCompleted(newState);
            if (!task.getSubtasks().isEmpty()) {
                boolean allDone = task.getSubtasks().stream().allMatch(Subtask::isCompleted);
                if (allDone && task.getStatus() != TaskStatus.COMPLETED) {
                    nextTaskStatus = TaskStatus.COMPLETED;
                    task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), TaskStatus.COMPLETED, task.getDueDate());
                    checkRecurringOnComplete(task);
                    showToast("Completed: “" + task.getTitle() + "” (all subtasks done)", true);
                } else if (!allDone && task.getStatus() == TaskStatus.COMPLETED) {
                    nextTaskStatus = TaskStatus.PENDING;
                    task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), TaskStatus.PENDING, task.getDueDate());
                    showToast("Marked pending: “" + task.getTitle() + "”", true);
                }
            }
            saveAndRefresh();
        }
        @Override public void undo() {
            subtask.setCompleted(!newState);
            if (nextTaskStatus != null) {
                task.update(task.getTitle(), task.getDescription(), task.getCategory(), task.getPriority(), prevTaskStatus, task.getDueDate());
            }
            saveAndRefresh();
        }
        @Override public String getDescription() { return "Subtask “" + subtask.getTitle() + "”"; }
    }
}
