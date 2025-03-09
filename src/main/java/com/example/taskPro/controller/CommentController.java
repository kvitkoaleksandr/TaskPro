package com.example.taskPro.controller;

import com.example.taskPro.model.Comment;
import com.example.taskPro.security.JwtUtil;
import com.example.taskPro.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment API", description = "Управление комментариями к задачам")
@RestController
@RequestMapping("/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Добавить комментарий", description = "Позволяет добавить комментарий к задаче.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Нет прав для комментирования задачи"),
            @ApiResponse(responseCode = "404", description = "Задача или пользователь не найдены")
    })
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long taskId,
            @RequestParam String content,
            Authentication authentication) {

        Long userId = jwtUtil.extractUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(commentService.addComment(taskId, userId, content));
    }

    @Operation(summary = "Получить комментарии задачи", description = "Позволяет получить все комментарии к задаче.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарии успешно получены"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }
}