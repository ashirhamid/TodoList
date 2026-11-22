# TASKNEST: JAVA TASK MANAGEMENT SYSTEM
## Software Construction Open-Ended Lab (OEL) Report | CLO-4 & CLO-5

**Course / Lab:** Software Construction — Open-Ended Lab (OEL)  
**Student 1 (Author):** Muhammad Mehroz Ammar (Reg No: 534699) [GitHub: Mehroz485]  
**Student 2 (Partner):** Ashir Hamid [GitHub: ashirhamid]  
**Submission Date:** November 22, 2026  
**Repository Remote:** `https://github.com/ashirhamid/TodoList.git`  
**Branch:** `main`

---

## 1. Objective & Learning Outcomes

This Open-Ended Lab (OEL) project executes a full, production-grade software construction lifecycle for an extensible task management desktop application built using modern Java tools.

- **CLO-4 (PLO-5, Weight 25%):** Practice and adapt modern software engineering tools including OpenJDK 21 LTS, Git version control, GitHub remote hosting, and the Javadoc HTML generation engine.
- **CLO-5 (PLO-5, Weight 75%):** Independently build a fully-functional, robust Java GUI application utilizing object-oriented principles, GoF design patterns (Command, Memento, Composite, Repository), multi-commit Git versioning, and rigorous test case verification.

---

## 2. Problem Description & Functional Requirements

Task management systems require balancing intuitive user interfaces with extensible architecture, crash resilience, and deep data validation. TaskNest addresses these challenges through modern desktop engineering:

1. **Full CRUD Operations:** Users can create, view, modify, and delete tasks with comprehensive validation (preventing empty titles or corrupt states).
2. **Metadata & Taxonomy:** Tasks feature Categorization (*Work, Study, Personal, Fitness, Errand*), Priority levels (*Urgent, High, Medium, Low*), and Status tracking (*Pending, In Progress, Completed*).
3. **Smart Due-Date Badges & Urgency Filters:** Real-time comparison against `LocalDate.now()` with color-coded badges (`🔴 Overdue`, `🟡 Due Today`, `🟢 Upcoming`) and dedicated sidebar filter pills.
4. **Live Productivity Dashboard:** A reactive progress dashboard calculating real-time completion percentage, task distribution, and urgency counters via Java Streams.
5. **Composite Subtasks Micro-Checklists:** Multi-item subtask checklists per task featuring bidirectional synchronization (completing parent auto-completes subtasks; completing all subtasks auto-completes parent).
6. **Smart Recurring Scheduling:** Automated next-occurrence date generation (*Daily, Weekly, Monthly*) triggered seamlessly upon task completion.
7. **Undo/Redo System:** Robust Undo/Redo mechanism implemented via Gang of Four Command & Memento patterns with global shortcuts (`Ctrl+Z` / `Ctrl+Y`) and an interactive UI toast.
8. **Data Interoperability:** One-click report generation exporting tasks to Markdown (`.md`) checklists and spreadsheet-ready CSV (`.csv`) formats.

---

## 3. Software Architecture & Design Patterns

TaskNest adheres strictly to SOLID design principles and standard GoF (Gang of Four) architectural patterns:

- **Model-View-Controller (MVC):** Separation of Concerns between data models (`Task`, `Subtask`, `Recurrence`), persistence layer (`TaskRepository`), and presentation layer (`TaskManagerPanel`, `ColorWashPanel`, `SoftButton`).
- **Command Pattern:** Encapsulates task operations (`AddTaskCommand`, `DeleteTaskCommand`, `UpdateTaskCommand`, `ToggleTaskCommand`, `ToggleSubtaskCommand`) into executable `TaskCommand` objects with forward and reverse execution.
- **Memento Pattern:** Captures and externalizes task states before destructive operations, allowing full rollback without violating encapsulation (`TaskMemento`).
- **Composite Pattern:** Treats individual subtask checklist items and composite task objects uniformly, enabling recursive status updates.
- **Repository Pattern:** Abstracts the JSON persistence layer, separating file I/O and schema serialization from the application business logic.

---

## 4. Git Version Control & Commit Revisions (CLO-5)

The project repository was maintained with disciplined Git version control, demonstrating granular, incremental feature development authored by **Muhammad Mehroz Ammar (Mehroz485)**:

| Commit Hash | Author | Commit Message / Scope |
| :--- | :--- | :--- |
| `82e98d7` | Mehroz485 | chore: add Windows batch launcher script with JDK auto-detection |
| `b1027e5` | Mehroz485 | refactor(meta): update author metadata to Muhammad Mehroz Ammar (534699) and Ashir Hamid |
| `fbab426` | Mehroz485 | feat(recurring): implement recurrence schedule engine (Daily, Weekly, Monthly) |
| `f922adf` | Mehroz485 | feat(subtasks): implement subtask hierarchy with bidirectional completion sync |
| `b50e3bd` | Mehroz485 | feat(history): implement Undo/Redo architecture using GoF Command Pattern and Memento |
| `c6020fe` | Mehroz485 | feat(persistence): integrate subtasks, recurrence, and JSON serialization in TaskRepository |
| `3087aae` | Mehroz485 | feat(ui): add urgency badges, live productivity progress bar, filters, and export |
| `90fa01d` | Mehroz485 | docs: update SRS architecture document, Javadoc API reference, and release JAR |

---

## 5. Code Documentation (Javadoc HTML Generation)

Full API documentation was authored using standard Javadoc tags (`@author`, `@param`, `@return`, `@throws`) across all source classes. The HTML documentation bundle was generated via JDK 21 javadoc and is located in `docs/javadoc/index.html`.

- **Authorship Tags:** `@author Muhammad Mehroz Ammar (534699), Ashir Hamid`
- **HTML Search Engine:** Search index, class hierarchy trees, and package indexes generated cleanly with 0 compilation errors.

---

## 6. Test Cases & Execution Results (CLO-5 Rubric)

| Test ID | Test Description | Expected Result | Actual Result & Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Add a valid task with full metadata | Task added to repository and rendered in list | Task successfully created; dashboard counters update. **[PASS]** |
| **TC-02** | Add a task with empty/blank title | Validation error displayed; task rejected | Warning dialog shown: *Title cannot be blank*. **[PASS]** |
| **TC-03** | View all tasks with category filtering | Tasks correctly filtered by selected category | Filter pills display accurate matching tasks. **[PASS]** |
| **TC-04** | View empty list state | Graceful empty state notification displayed | Clean empty state illustration shown. **[PASS]** |
| **TC-05** | Update existing task details | Changes persisted and UI reflects new state | All fields updated smoothly in storage and UI. **[PASS]** |
| **TC-06** | Update non-existing task | Handled gracefully without crashing | Handled safely; no null pointer exceptions. **[PASS]** |
| **TC-07** | Delete an existing task | Task removed from storage and UI | Task removed; interactive Undo banner displayed. **[PASS]** |
| **TC-08** | Subtasks Two-Way Sync (Composite) | Parent ticks subtasks; subtasks tick parent | Verified: all 4 sync conditions evaluate to true. **[PASS]** |
| **TC-09** | Recurring Task Scheduling Engine | Completing task spawns next recurrence | New task generated with updated due date. **[PASS]** |
| **TC-10** | Command Undo/Redo Engine | Ctrl+Z reverts operation; Ctrl+Y restores | Command stack restores previous state accurately. **[PASS]** |
| **TC-11** | Data Interoperability Export | Export to Markdown (`.md`) and CSV (`.csv`) | Generated valid Markdown and Excel CSV files. **[PASS]** |

---

## 7. Application Screenshots & Viva Demonstration Guide

During the physical viva and evaluation (starting 23 November), launch `run.bat` or `dist/TaskNest.jar` to demonstrate:

1. **Figure 1 — Main Dashboard & Live Statistics:** Shows the Live Productivity Progress Bar, Urgency Badges (`🔴 Overdue`, `🟡 Due Today`), and Category Sidebar.
2. **Figure 2 — Task Creation & Editor Modal:** Demonstrates input validation, Priority selection, Due Date picker, Recurrence dropdown, and Subtasks checklist editor.
3. **Figure 3 — Subtasks Micro-Checklist & 2-Way Sync:** Shows subtasks being auto-checked when the main task is ticked, and vice-versa.
4. **Figure 4 — Command Pattern Undo/Redo Toast:** Demonstrates deleting a task, the toast notification appearing with an Undo button, and restoring via `Ctrl+Z`.
5. **Figure 5 — Data Interoperability Export:** Shows exported `.md` checklist and `.csv` spreadsheet opened in Notepad / Excel.
