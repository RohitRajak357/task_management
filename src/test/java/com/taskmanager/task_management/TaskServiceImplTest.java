package com.taskmanager.task_management;

import com.taskmanager.task_management.dto.TaskRequest;
import com.taskmanager.task_management.exception.TaskNotFoundException;
import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;
import com.taskmanager.task_management.repository.TaskRepository;
import com.taskmanager.task_management.service.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;
    private TaskRequest sampleRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Reusable sample task
        sampleTask = new Task();
        sampleTask.setId("test-id-123");
        sampleTask.setTitle("Sample Task");
        sampleTask.setDescription("Sample Description");
        sampleTask.setStatus(TaskStatus.PENDING);
        sampleTask.setDueDate(LocalDate.now().plusDays(5));

        // Reusable sample request
        sampleRequest = new TaskRequest();
        sampleRequest.setTitle("Sample Task");
        sampleRequest.setDescription("Sample Description");
        sampleRequest.setStatus(TaskStatus.PENDING);
        sampleRequest.setDueDate(LocalDate.now().plusDays(5));
    }

    // ─── CREATE TASK ────────────────────────────────────────

    @Test
    void createTask_shouldCreateAndReturnTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task result = taskService.createTask(sampleRequest);

        assertNotNull(result);
        assertEquals("Sample Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_shouldGenerateUniqueId() {
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result1 = taskService.createTask(sampleRequest);
        Task result2 = taskService.createTask(sampleRequest);

        assertNotNull(result1.getId());
        assertNotNull(result2.getId());
        assertNotEquals(result1.getId(), result2.getId()); // IDs must be unique
    }

    @Test
    void createTask_shouldDefaultStatusToPendingWhenNotProvided() {
        sampleRequest.setStatus(null); // No status provided
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(sampleRequest);

        assertEquals(TaskStatus.PENDING, result.getStatus());
    }

    @Test
    void createTask_shouldRespectProvidedStatus() {
        sampleRequest.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(sampleRequest);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void createTask_shouldHandleNullDescription() {
        sampleRequest.setDescription(null);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(sampleRequest);

        assertNull(result.getDescription());
    }

    // ─── GET TASK BY ID ─────────────────────────────────────

    @Test
    void getTaskById_shouldReturnTaskWhenExists() {
        when(taskRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTask));

        Task result = taskService.getTaskById("test-id-123");

        assertNotNull(result);
        assertEquals("test-id-123", result.getId());
        assertEquals("Sample Task", result.getTitle());
    }

    @Test
    void getTaskById_shouldThrowTaskNotFoundExceptionWhenNotExists() {
        when(taskRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById("nonexistent-id"));

        assertTrue(exception.getMessage().contains("nonexistent-id"));
    }

    @Test
    void getTaskById_shouldThrowExceptionForEmptyStringId() {
        when(taskRepository.findById("")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById(""));
    }

    // ─── GET ALL TASKS ──────────────────────────────────────

    @Test
    void getAllTasks_shouldReturnAllTasksWhenNoStatusFilter() {
        List<Task> tasks = Arrays.asList(sampleTask, sampleTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks(null);

        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
        verify(taskRepository, never()).findByStatus(any());
    }

    @Test
    void getAllTasks_shouldReturnFilteredTasksWhenStatusProvided() {
        List<Task> pendingTasks = Collections.singletonList(sampleTask);
        when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(pendingTasks);

        List<Task> result = taskService.getAllTasks(TaskStatus.PENDING);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.PENDING);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasksExist() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasks(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasksMatchStatus() {
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasks(TaskStatus.DONE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── UPDATE TASK ────────────────────────────────────────

    @Test
    void updateTask_shouldUpdateAllFieldsWhenAllProvided() {
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus(TaskStatus.DONE);
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        when(taskRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask("test-id-123", updateRequest);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    void updateTask_shouldKeepExistingValuesWhenFieldsAreNull() {
        TaskRequest partialRequest = new TaskRequest();
        partialRequest.setTitle(null);       // not updating title
        partialRequest.setDescription(null); // not updating description
        partialRequest.setStatus(null);      // not updating status
        partialRequest.setDueDate(null);     // not updating dueDate

        when(taskRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask("test-id-123", partialRequest);

        // Should keep original values
        assertEquals("Sample Task", result.getTitle());
        assertEquals("Sample Description", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
    }

    @Test
    void updateTask_shouldPreserveOriginalId() {
        when(taskRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask("test-id-123", sampleRequest);

        assertEquals("test-id-123", result.getId()); // ID must never change
    }

    @Test
    void updateTask_shouldThrowTaskNotFoundExceptionWhenNotExists() {
        when(taskRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask("nonexistent-id", sampleRequest));
    }

    @Test
    void updateTask_shouldCallSaveOnce() {
        when(taskRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.updateTask("test-id-123", sampleRequest);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // ─── DELETE TASK ────────────────────────────────────────

    @Test
    void deleteTask_shouldDeleteWhenTaskExists() {
        when(taskRepository.existsById("test-id-123")).thenReturn(true);
        doNothing().when(taskRepository).deleteById("test-id-123");

        assertDoesNotThrow(() -> taskService.deleteTask("test-id-123"));
        verify(taskRepository, times(1)).deleteById("test-id-123");
    }

    @Test
    void deleteTask_shouldThrowTaskNotFoundExceptionWhenNotExists() {
        when(taskRepository.existsById("nonexistent-id")).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTask("nonexistent-id"));

        assertTrue(exception.getMessage().contains("nonexistent-id"));
        verify(taskRepository, never()).deleteById(any()); // delete should never be called
    }

    @Test
    void deleteTask_shouldNotCallDeleteWhenTaskNotFound() {
        when(taskRepository.existsById("nonexistent-id")).thenReturn(false);

        assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTask("nonexistent-id"));

        verify(taskRepository, never()).deleteById("nonexistent-id");
    }
}