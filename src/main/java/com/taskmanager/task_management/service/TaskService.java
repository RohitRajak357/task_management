package com.taskmanager.task_management.service;

import com.taskmanager.task_management.dto.TaskRequest;
import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;

import java.util.List;

public interface TaskService {

    Task createTask(TaskRequest request);

    Task getTaskById(String id);

    List<Task> getAllTasks(TaskStatus status);

    Task updateTask(String id, TaskRequest request);

    void deleteTask(String id);
}