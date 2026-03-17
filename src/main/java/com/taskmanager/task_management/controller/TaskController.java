package com.taskmanager.task_management.controller;

import com.taskmanager.task_management.dto.TaskRequest;
import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;
import com.taskmanager.task_management.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor injection (replaces @RequiredArgsConstructor)
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /tasks - Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request) {
        Task createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // GET /tasks/{id} - Get a task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // GET /tasks - Get all tasks (optional filter by status)
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) TaskStatus status) {
        List<Task> tasks = taskService.getAllTasks(status);
        return ResponseEntity.ok(tasks);
    }

    // PUT /tasks/{id} - Update a task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id,
                                           @Valid @RequestBody TaskRequest request) {
        Task updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE /tasks/{id} - Delete a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}