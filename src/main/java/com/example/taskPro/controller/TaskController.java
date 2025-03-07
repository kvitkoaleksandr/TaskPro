package com.example.taskPro.controller;

import com.example.taskPro.model.Task;
import com.example.taskPro.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    // Получить все задачи (Любой авторизованный пользователь)
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // Получить задачу по ID (Любой авторизованный пользователь)
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Создать задачу (Только ADMIN)
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, Authentication authentication) {
        String adminEmail = authentication.getName(); // Получаем email ADMIN из токена
        return ResponseEntity.ok(taskService.createTask(task, adminEmail));
    }

    // Обновить задачу (Только ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask, Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(taskService.updateTask(id, updatedTask, adminEmail));
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
