package com.taskmanager.task_management.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class Task {

    private String id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    private TaskStatus status;

    @NotNull(message = "Due date is mandatory")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    // Constructors
    public Task() {}

    public Task(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // Builder
    public static TaskBuilder builder() { return new TaskBuilder(); }

    public static class TaskBuilder {
        private String id;
        private String title;
        private String description;
        private TaskStatus status;
        private LocalDate dueDate;

        public TaskBuilder id(String id) { this.id = id; return this; }
        public TaskBuilder title(String title) { this.title = title; return this; }
        public TaskBuilder description(String description) { this.description = description; return this; }
        public TaskBuilder status(TaskStatus status) { this.status = status; return this; }
        public TaskBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }

        public Task build() {
            return new Task(id, title, description, status, dueDate);
        }
    }
}