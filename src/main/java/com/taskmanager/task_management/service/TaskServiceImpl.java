package com.taskmanager.task_management.service;

import com.taskmanager.task_management.dto.TaskRequest;
import com.taskmanager.task_management.exception.TaskNotFoundException;
import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;
import com.taskmanager.task_management.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(TaskRequest request) {
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public List<Task> getAllTasks(TaskStatus status) {
        if (status != null) {
            return taskRepository.findByStatus(status);
        }
        return taskRepository.findAll();
    }

    @Override
    public Task updateTask(String id, TaskRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        Task updatedTask = Task.builder()
                .id(existingTask.getId())
                .title(request.getTitle() != null ? request.getTitle() : existingTask.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : existingTask.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : existingTask.getStatus())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : existingTask.getDueDate())
                .build();

        return taskRepository.save(updatedTask);
    }

    @Override
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}