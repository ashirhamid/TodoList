# TaskNest Requirements and Design

## Problem

TaskNest provides a small desktop space for recording everyday work and tracking each task from pending to complete. A user can create and maintain tasks, group them with categories, assign priority, and find tasks again through search and filters.

## Requirements

Each task must have a non-empty title, a description, a category, a priority, and a status. A due date is optional. Users can add, open, edit, remove, and reprioritize tasks. The application rejects blank titles and categories and gives a clear message for an invalid due date. Task and category data persist locally between launches.

## Design decisions

The interface is a Java Swing desktop application. Swing keeps the project self-contained, while the separated model, repository, editor, and main panel keep UI code distinct from stored task data. The repository uses Java properties with Base64-encoded free-text fields so line breaks and punctuation remain safe to store. It writes to a temporary file and replaces the previous data file atomically when the file system supports it.

The task list sorts high-priority tasks before medium- and low-priority tasks. Tasks with the same priority are ordered by due date, with tasks without a due date at the end, followed by creation time. Search covers title, description, and category. Status and category filters narrow the list without changing saved task values.

## User interface

The application uses a textured cream-to-blue background wash with navy text. Rounded cream navigation and action controls sit lightly over the canvas, and Georgia display text pairs with Palatino Linotype for the body and controls. Task cards show the title, description, category, priority, status, and due date. The task editor follows the same palette and keeps the required fields together while validating input before saving.

## CLO evidence

- **CLO 2 / PLO 2 / WA4:** The project expresses the task problem as requirements, models the task data, validates user input, and applies explicit priority and due-date sorting rules. These choices provide material for the student's investigation and design explanation.
- **CLO 4 / PLO 5 / WA5:** The project can be built with the JDK, opened in a Java IDE, tracked with Git, and documented with generated Javadoc.

## Main classes

`Task` owns task fields and validation. `Category`, `Priority`, and `TaskStatus` represent the organizing and workflow concepts. `TaskRepository` handles local persistence. `TaskEditorDialog` gathers validated input. `TaskManagerPanel` renders the interface and coordinates operations. `TaskNestApp` starts the program.
