package com.taskmanager.task_management.repository;

import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(String id);

    List<Task> findAll();

    List<Task> findByStatus(TaskStatus status);

    void deleteById(String id);

    boolean existsById(String id);
}