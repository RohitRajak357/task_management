package com.taskmanager.task_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.task_management.controller.TaskController;
import com.taskmanager.task_management.dto.TaskRequest;
import com.taskmanager.task_management.exception.GlobalExceptionHandler;
import com.taskmanager.task_management.exception.TaskNotFoundException;
import com.taskmanager.task_management.model.Task;
import com.taskmanager.task_management.model.TaskStatus;
import com.taskmanager.task_management.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task sampleTask;
    private TaskRequest sampleRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup MockMvc with our controller and exception handler
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // ObjectMapper handles Java <-> JSON conversion
        // JavaTimeModule handles LocalDate serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

    // ─── CREATE TASK (POST /tasks) ───────────────────────────

    @Test
    void createTask_shouldReturn201AndCreatedTask() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createTask_shouldReturn400WhenTitleIsMissing() throws Exception {
        sampleRequest.setTitle(null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is mandatory"));
    }

    @Test
    void createTask_shouldReturn400WhenTitleIsBlank() throws Exception {
        sampleRequest.setTitle("   "); // blank string

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is mandatory"));
    }

    @Test
    void createTask_shouldReturn400WhenDueDateIsMissing() throws Exception {
        sampleRequest.setDueDate(null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dueDate").value("Due date is mandatory"));
    }

    @Test
    void createTask_shouldReturn400WhenDueDateIsInThePast() throws Exception {
        sampleRequest.setDueDate(LocalDate.now().minusDays(1)); // past date

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dueDate").value("Due date must be in the future"));
    }

    @Test
    void createTask_shouldReturn400WhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET TASK BY ID (GET /tasks/{id}) ───────────────────

    @Test
    void getTaskById_shouldReturn200AndTask() throws Exception {
        when(taskService.getTaskById("test-id-123")).thenReturn(sampleTask);

        mockMvc.perform(get("/tasks/test-id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.description").value("Sample Description"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getTaskById_shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById("nonexistent-id"))
                .thenThrow(new TaskNotFoundException("nonexistent-id"));

        mockMvc.perform(get("/tasks/nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found with id: nonexistent-id"));
    }

    @Test
    void getTaskById_shouldReturn404ForRandomId() throws Exception {
        when(taskService.getTaskById(any())).thenThrow(new TaskNotFoundException("random-id"));

        mockMvc.perform(get("/tasks/random-id"))
                .andExpect(status().isNotFound());
    }

    // ─── GET ALL TASKS (GET /tasks) ──────────────────────────

    @Test
    void getAllTasks_shouldReturn200AndAllTasks() throws Exception {
        when(taskService.getAllTasks(null)).thenReturn(Arrays.asList(sampleTask, sampleTask));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllTasks_shouldReturn200AndEmptyListWhenNoTasks() throws Exception {
        when(taskService.getAllTasks(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllTasks_shouldFilterByStatusWhenProvided() throws Exception {
        when(taskService.getAllTasks(TaskStatus.PENDING))
                .thenReturn(Collections.singletonList(sampleTask));

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasksMatchStatus() throws Exception {
        when(taskService.getAllTasks(TaskStatus.DONE)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── UPDATE TASK (PUT /tasks/{id}) ───────────────────────

    @Test
    void updateTask_shouldReturn200AndUpdatedTask() throws Exception {
        Task updatedTask = new Task();
        updatedTask.setId("test-id-123");
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setDueDate(LocalDate.now().plusDays(10));

        when(taskService.updateTask(eq("test-id-123"), any(TaskRequest.class)))
                .thenReturn(updatedTask);

        sampleRequest.setTitle("Updated Title");
        sampleRequest.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/tasks/test-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.updateTask(eq("nonexistent-id"), any(TaskRequest.class)))
                .thenThrow(new TaskNotFoundException("nonexistent-id"));

        mockMvc.perform(put("/tasks/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found with id: nonexistent-id"));
    }

    @Test
    void updateTask_shouldReturn400WhenTitleIsBlank() throws Exception {
        sampleRequest.setTitle("");

        mockMvc.perform(put("/tasks/test-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE TASK (DELETE /tasks/{id}) ────────────────────

    @Test
    void deleteTask_shouldReturn204WhenSuccessful() throws Exception {
        doNothing().when(taskService).deleteTask("test-id-123");

        mockMvc.perform(delete("/tasks/test-id-123"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask("test-id-123");
    }

    @Test
    void deleteTask_shouldReturn404WhenTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException("nonexistent-id"))
                .when(taskService).deleteTask("nonexistent-id");

        mockMvc.perform(delete("/tasks/nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found with id: nonexistent-id"));
    }

    @Test
    void deleteTask_shouldNotCallServiceTwice() throws Exception {
        doNothing().when(taskService).deleteTask("test-id-123");

        mockMvc.perform(delete("/tasks/test-id-123"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask("test-id-123"); // exactly once
    }
}