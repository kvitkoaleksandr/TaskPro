package com.example.taskPro.controller;

import com.example.taskPro.model.Task;
import com.example.taskPro.model.User;
import com.example.taskPro.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/filter")
    public ResponseEntity<Page<Task>> getTasksByAuthorOrExecutorId(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long executorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Task> tasks = taskService.getTasksFiltered(authorId, executorId, page, size);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.createTask(task, admin.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                           @Valid @RequestBody Task updatedTask,
                                           Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.updateTask(id, updatedTask, admin.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        taskService.deleteTask(id, admin.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignExecutor(@PathVariable Long id,
                                               @RequestParam Long executorId,
                                               Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.assignExecutor(id, executorId, admin.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status, user.getId()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Task> addComment(@PathVariable Long id,
                                           @RequestParam String comment,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.addComment(id, comment, user.getId()));
    }
}