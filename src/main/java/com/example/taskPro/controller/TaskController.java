package com.example.taskPro.controller;

import com.example.taskPro.model.Task;
import com.example.taskPro.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task API",
        description = "Управление задачами (создание, редактирование, удаление, "
                + "назначение исполнителя и изменение статуса)")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Получить список задач",
            description = "Фильтрация по автору или исполнителю, поддерживается пагинация.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к ресурсу")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<Task>> getTasksByAuthorOrExecutorId(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long executorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.getTasksFiltered(authorId, executorId, page, size));
    }

    @Operation(summary = "Создать задачу", description = "Администратор создаёт новую задачу.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для создания задачи"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(task, authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Изменить статус задачи", description = "User может менять статус только своих задач.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для изменения статуса"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректный статус")
    })
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id,
                                                 @RequestParam String status, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status, authentication));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Изменить приоритет задачи", description = "Admin может изменять приоритет любой задачи.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Приоритет успешно обновлен"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для изменения приоритета"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректный приоритет")
    })
    public ResponseEntity<Task> updateTaskPriority(@PathVariable Long id,
                                                   @RequestParam String priority, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTaskPriority(id, priority, authentication));
    }

    @Operation(summary = "Удалить задачу", description = "Администратор удаляет задачу.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача удалена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа для удаления"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить задачи автора",
            description = "Позволяет администратору видеть свои созданные задачи.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<Task>> getTasksByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getTasksByAuthor(authorId, page, size));
    }

    @Operation(summary = "Получить задачи исполнителя",
            description = "Позволяет получить задачи, где пользователь указан исполнителем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    @GetMapping("/executor/{executorId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Page<Task>> getTasksByExecutor(
            @PathVariable Long executorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getTasksByExecutor(executorId, page, size));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Task> assignExecutor(@PathVariable Long id,
                                               @RequestParam Long executorId, Authentication authentication) {
        return ResponseEntity.ok(taskService.assignExecutor(id, executorId, authentication));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Task> addComment(@PathVariable Long id,
                                           @RequestParam String comment, Authentication authentication) {
        return ResponseEntity.ok(taskService.addComment(id, comment, authentication));
    }
}