package com.example.taskPro.controller;

import com.example.taskPro.model.Task;
import com.example.taskPro.model.User;
import com.example.taskPro.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return ResponseEntity.ok(taskService.getTasksFiltered(authorId, executorId, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(task, ((User) authentication.getPrincipal()).getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                           @Valid @RequestBody Task updatedTask, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(
                id,
                updatedTask,
                ((User) authentication.getPrincipal()).getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, ((User) authentication.getPrincipal()).getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Task> assignExecutor(@PathVariable Long id,
                                               @RequestParam Long executorId, Authentication authentication) {
        return ResponseEntity.ok(taskService.assignExecutor(
                id,
                executorId,
                ((User) authentication.getPrincipal()).getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id,
                                             @RequestParam String status, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTaskStatus(
                id,
                status,
                ((User) authentication.getPrincipal()).getId()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Task> addComment(@PathVariable Long id,
                                           @RequestParam String comment, Authentication authentication) {
        return ResponseEntity.ok(taskService.addComment(
                id,
                comment,
                ((User) authentication.getPrincipal()).getId()));
    }
}