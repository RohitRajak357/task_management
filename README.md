# Task Management API

A RESTful Task Management System built with Java Spring Boot, following Domain-Driven Design (DDD) principles and Test-Driven Development (TDD).

---

## Tech Stack

- Java 17
- Spring Boot 3.5.11
- Spring Web (REST API)
- Spring Validation (Bean Validation)
- Maven (Dependency Management)
- JUnit 5 (Unit Testing)
- Mockito (Mocking)
- MockMvc (Controller Testing)

---

## Project Structure
```
src/
├── main/java/com/taskmanager/task_management/
│   ├── controller/        # REST Controllers (HTTP layer)
│   ├── service/           # Business logic layer
│   ├── repository/        # In-memory data storage layer
│   ├── model/             # Task entity and TaskStatus enum
│   ├── dto/               # Data Transfer Objects (request bodies)
│   └── exception/         # Custom exceptions and global handler
└── test/java/com/taskmanager/task_management/
    ├── TaskControllerTest.java
    ├── TaskServiceImplTest.java
    └── InMemoryTaskRepositoryTest.java
```

---

## Prerequisites

Make sure you have the following installed:

- [Java 17+](https://www.oracle.com/java/technologies/downloads/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)

Verify installations:
```bash
java -version
mvn -version
```

---

## How to Run

### 1. Clone or extract the project
```bash
cd task-management
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The server will start on **http://localhost:8080**

---

## API Endpoints

### Create Task
```
POST /tasks
Content-Type: application/json

{
  "title": "My Task",
  "description": "Task description",
  "status": "PENDING",
  "dueDate": "2026-08-01"
}
```

### Get Task by ID
```
GET /tasks/{id}
```

### Get All Tasks
```
GET /tasks
GET /tasks?status=PENDING
GET /tasks?status=IN_PROGRESS
GET /tasks?status=DONE
```

### Update Task
```
PUT /tasks/{id}
Content-Type: application/json

{
  "title": "Updated Task",
  "status": "IN_PROGRESS",
  "dueDate": "2026-09-01"
}
```

### Delete Task
```
DELETE /tasks/{id}
```

---

## Task Model

| Field       | Type    | Required | Description                          |
|-------------|---------|----------|--------------------------------------|
| id          | String  | Auto     | Auto-generated UUID                  |
| title       | String  | Yes      | Title of the task                    |
| description | String  | No       | Optional description                 |
| status      | Enum    | No       | PENDING, IN_PROGRESS, DONE (default: PENDING) |
| dueDate     | Date    | Yes      | Must be a future date (YYYY-MM-DD)   |

---

## Validation Rules

- `title` — mandatory, cannot be blank
- `dueDate` — mandatory, must be a future date
- `status` — optional, defaults to `PENDING` if not provided

---

## Error Responses

### 404 Not Found
```json
{
  "error": "Task not found with id: {id}"
}
```

### 400 Bad Request
```json
{
  "title": "Title is mandatory",
  "dueDate": "Due date must be in the future"
}
```

---

## Running Tests
```bash
mvn test
```

**Test coverage:**
- `InMemoryTaskRepositoryTest` — 15 tests
- `TaskServiceImplTest` — 18 tests
- `TaskControllerTest` — 18 tests
- `TaskManagementApplicationTests` — 7 tests
- **Total: 58 tests**

---

## Design Principles

- **Domain-Driven Design (DDD)** — Code separated into Controller, Service, Repository, and Model layers
- **Test-Driven Development (TDD)** — Comprehensive unit tests for all components
- **Clean Code** — Meaningful names, single responsibility, interface-based design
- **In-Memory Storage** — Uses HashMap for data persistence (no database required)