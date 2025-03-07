package com.example.taskPro.controller;

import com.example.taskPro.model.Task;
import com.example.taskPro.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/filter")
    public ResponseEntity<List<Task>> getTasksByAuthorOrExecutor(
            @RequestParam(required = false) String authorEmail,
            @RequestParam(required = false) String executorEmail
    ) {
        List<Task> tasks = taskService.getTasksByAuthorOrExecutor(authorEmail, executorEmail);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(taskService.createTask(task, adminEmail));
    }
    // Получить все задачи (Любой авторизованный пользователь)

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                           @Valid @RequestBody Task updatedTask, Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(taskService.updateTask(id, updatedTask, adminEmail));
    }

    @GetMapping
    public Page<Task> getTasks(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long executorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasksFiltered(authorId, executorId, page, size);
    }

    // Получить задачу по ID (Любой авторизованный пользователь)

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id); // Получаем задачу
        return ResponseEntity.ok(task);
    }

    // Удалить задачу (Только ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        String adminEmail = authentication.getName();
        taskService.deleteTask(id, adminEmail);
        return ResponseEntity.noContent().build();
    }

    // Назначить исполнителя (Только ADMIN)
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignExecutor(@PathVariable Long id, @RequestParam String executorEmail, Authentication authentication) {
        String adminEmail = authentication.getName(); // Получаем email ADMIN
        return ResponseEntity.ok(taskService.assignExecutor(id, executorEmail, adminEmail));
    }

    // Изменить статус задачи (Только исполнитель задачи)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        String userEmail = authentication.getName(); // Получаем email текущего пользователя
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status, userEmail));
    }

    // Добавить комментарий (Любой авторизованный пользователь)
    @PostMapping("/{id}/comments")
    public ResponseEntity<Task> addComment(@PathVariable Long id, @RequestParam String comment, Authentication authentication) {
        String userEmail = authentication.getName(); // Получаем email текущего пользователя
        return ResponseEntity.ok(taskService.addComment(id, comment, userEmail));
    }
}
