# TaskNest

TaskNest is a Java Swing desktop TODO application for creating, organizing, prioritizing, and tracking personal tasks. Its canvas blends textured cream into soft blue, with a muted blue title bar, rounded cream controls, and Georgia and Palatino Linotype typography. It stores data locally and needs no third-party libraries.

## Learning outcome alignment

| CLO | PLO | Washington Accord attribute | Application evidence |
| --- | --- | --- | --- |
| CLO 2 | PLO 2 | WA4 Investigations | Requirements are translated into task data, validation rules, filters, sorting, and a persistence design. Students can explain the design choices and trade-offs in their report. |
| CLO 4 | PLO 5 | WA5 Modern Tool Usage | The project uses a JDK, an IDE, Git, and the Javadoc tool to build, manage, and document the application. |

## Features

- Create, view, edit, and delete tasks.
- Undo / Redo system (`Ctrl+Z` / `Ctrl+Y`) with interactive toast notification (Command Pattern).
- Interactive sub-tasks / micro-checklists inside tasks with bidirectional synchronization (Composite Pattern).
- Smart recurring tasks (Daily, Weekly, Monthly) with automated next-run scheduling upon completion.
- Set a task title, description, category, priority, status, and optional due date.
- Mark tasks complete from the task list.
- Search tasks and filter by status, priority, or category.
- Dynamic due-date badges (`🔴 Overdue`, `🟡 Due Today`, `🟢 Upcoming`) and dedicated "Due Today" / "Overdue" filters.
- Live productivity progress bar with dynamic completion percentage and urgency metrics.
- One-click data export to Markdown checklist (`.md`) and spreadsheet (`.csv`) formats.
- Sort higher-priority tasks first, then earlier due dates.
- Reject blank titles and categories, and validate due dates in `YYYY-MM-DD` format.
- Save tasks and categories automatically to the current user's `.tasknest/tasks.properties` file.
- Keep saved task data when the application is closed and reopened.

## Requirements

- JDK 17 or newer
- Windows PowerShell, or an IDE such as IntelliJ IDEA, Eclipse, or NetBeans

The project uses only Java's standard library, including Swing, `java.time`, and `java.util.Properties`.

## Run on Windows

Open PowerShell in this project folder and run:

```powershell
./run.ps1
```

The script compiles the sources into `build/classes` and opens the desktop application. In an IDE, import the folder as a Java project, mark `src/main/java` as a source root, and run `edu.oel.tasknest.TaskNestApp`.

For a quick launch with Java installed, run the packaged application:

```powershell
java -jar .\dist\TaskNest.jar
```

## Generate Javadoc

```powershell
./generate-javadoc.ps1
```

The generated HTML appears in `docs/javadoc`. Review it in a browser before submission. Add the team members' names to the `@author` tags before handing in the project.

## Architecture

| Class | Responsibility |
| --- | --- |
| `Task` | Holds task details and enforces the non-empty title rule. |
| `Category` | Represents a named task group with case-insensitive equality. |
| `Priority` | Defines Low, Medium, and High levels and their sort order. |
| `TaskStatus` | Defines Pending, In Progress, and Completed states. |
| `TaskRepository` | Loads and saves task data using UTF-8 properties and an atomic file replacement when supported. |
| `TaskEditorDialog` | Collects and validates task details. |
| `TaskManagerPanel` | Builds the interface, applies search and filters, and coordinates task changes. |
| `TaskNestApp` | Configures Swing and starts the application. |

## Git workflow

The project is tracked on the `main` branch. To continue recording meaningful changes:

```powershell
git add .
git status
git log --oneline
git diff
git commit -m "Describe your change"
git push origin main
```

The project repository is `ashirhamid/TodoList`. Do not include personal task data from `.tasknest`.

## Before submission

- Add both group members' names to the Javadoc author tags and report.
- Run the project in the IDE required by the course and capture your own application screenshots.
- Record the actual results of the course's required test cases; this project does not claim test execution results.
- Generate and review Javadoc, then include the final source and documentation in the course submission format.
