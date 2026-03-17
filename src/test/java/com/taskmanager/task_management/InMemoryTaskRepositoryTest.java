package com.taskmanager.task_management;

import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;
import com.taskmanager.task_management.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskRepositoryTest {

    private InMemoryTaskRepository repository;

    // Helper method to create a task quickly
    private Task buildTask(String id, String title, TaskStatus status, LocalDate dueDate) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Test description");
        task.setStatus(status);
        task.setDueDate(dueDate);
        return task;
    }

    @BeforeEach
    void setUp() {
        // Fresh repository before every single test
        repository = new InMemoryTaskRepository();
    }

    // ─── SAVE ───────────────────────────────────────────────

    @Test
    void save_shouldPersistTask() {
        Task task = buildTask("1", "Test Task", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        Task saved = repository.save(task);

        assertNotNull(saved);
        assertEquals("1", saved.getId());
        assertEquals("Test Task", saved.getTitle());
    }

    @Test
    void save_shouldOverwriteExistingTaskWithSameId() {
        Task task1 = buildTask("1", "Original Title", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        repository.save(task1);

        Task task2 = buildTask("1", "Updated Title", TaskStatus.DONE, LocalDate.now().plusDays(2));
        repository.save(task2);

        Optional<Task> result = repository.findById("1");
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals(TaskStatus.DONE, result.get().getStatus());
    }

    @Test
    void save_shouldHandleTaskWithNullDescription() {
        Task task = buildTask("1", "No Desc Task", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        task.setDescription(null);
        Task saved = repository.save(task);

        assertNotNull(saved);
        assertNull(saved.getDescription());
    }

    // ─── FIND BY ID ─────────────────────────────────────────

    @Test
    void findById_shouldReturnTaskWhenExists() {
        Task task = buildTask("1", "Test Task", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        repository.save(task);

        Optional<Task> result = repository.findById("1");

        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Task> result = repository.findById("nonexistent-id");
        assertFalse(result.isPresent());
    }

    @Test
    void findById_shouldReturnEmptyForNullId() {
        Optional<Task> result = repository.findById(null);
        assertFalse(result.isPresent());
    }

    // ─── FIND ALL ───────────────────────────────────────────

    @Test
    void findAll_shouldReturnEmptyListWhenNoTasks() {
        List<Task> result = repository.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllTasks() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(3)));
        repository.save(buildTask("2", "Task 2", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(1)));
        repository.save(buildTask("3", "Task 3", TaskStatus.DONE, LocalDate.now().plusDays(2)));

        List<Task> result = repository.findAll();
        assertEquals(3, result.size());
    }

    @Test
    void findAll_shouldReturnTasksSortedByDueDate() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(10)));
        repository.save(buildTask("2", "Task 2", TaskStatus.PENDING, LocalDate.now().plusDays(2)));
        repository.save(buildTask("3", "Task 3", TaskStatus.PENDING, LocalDate.now().plusDays(5)));

        List<Task> result = repository.findAll();

        assertEquals("2", result.get(0).getId()); // earliest due date first
        assertEquals("3", result.get(1).getId());
        assertEquals("1", result.get(2).getId()); // latest due date last
    }

    // ─── FIND BY STATUS ─────────────────────────────────────

    @Test
    void findByStatus_shouldReturnOnlyMatchingTasks() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        repository.save(buildTask("2", "Task 2", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2)));
        repository.save(buildTask("3", "Task 3", TaskStatus.PENDING, LocalDate.now().plusDays(3)));

        List<Task> result = repository.findByStatus(TaskStatus.PENDING);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getStatus() == TaskStatus.PENDING));
    }

    @Test
    void findByStatus_shouldReturnEmptyWhenNoMatchingStatus() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));

        List<Task> result = repository.findByStatus(TaskStatus.DONE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatus_shouldReturnTasksSortedByDueDate() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(5)));
        repository.save(buildTask("2", "Task 2", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        repository.save(buildTask("3", "Task 3", TaskStatus.PENDING, LocalDate.now().plusDays(3)));

        List<Task> result = repository.findByStatus(TaskStatus.PENDING);

        assertEquals("2", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        assertEquals("1", result.get(2).getId());
    }

    // ─── DELETE ─────────────────────────────────────────────

    @Test
    void deleteById_shouldRemoveTask() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        repository.deleteById("1");

        assertFalse(repository.existsById("1"));
    }

    @Test
    void deleteById_shouldNotThrowWhenIdDoesNotExist() {
        // Should silently do nothing
        assertDoesNotThrow(() -> repository.deleteById("nonexistent-id"));
    }

    @Test
    void deleteById_shouldOnlyDeleteSpecificTask() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        repository.save(buildTask("2", "Task 2", TaskStatus.PENDING, LocalDate.now().plusDays(2)));

        repository.deleteById("1");

        assertFalse(repository.existsById("1"));
        assertTrue(repository.existsById("2")); // Task 2 should still exist
    }

    // ─── EXISTS BY ID ───────────────────────────────────────

    @Test
    void existsById_shouldReturnTrueWhenTaskExists() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        assertTrue(repository.existsById("1"));
    }

    @Test
    void existsById_shouldReturnFalseWhenTaskDoesNotExist() {
        assertFalse(repository.existsById("nonexistent-id"));
    }

    @Test
    void existsById_shouldReturnFalseAfterDeletion() {
        repository.save(buildTask("1", "Task 1", TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        repository.deleteById("1");
        assertFalse(repository.existsById("1"));
    }
}