package com.example.taskPro.service;

import com.example.taskPro.exception.TaskNotFoundException;
import com.example.taskPro.exception.UnauthorizedActionException;
import com.example.taskPro.exception.UserNotFoundException;
import com.example.taskPro.model.Comment;
import com.example.taskPro.model.Role;
import com.example.taskPro.model.Task;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.CommentRepository;
import com.example.taskPro.repository.TaskRepository;
import com.example.taskPro.repository.UserRepository;
import com.example.taskPro.security.JwtUtil;
import com.example.taskPro.service.interfaces.CommentServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService implements CommentServiceInterface {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public Comment addComment(Long taskId, String content, Authentication authentication) {
        Long authorId = jwtUtil.getEntityIdFromAuth(authentication);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + authorId + " не найден"));

        // Проверяем права: админ может комментировать любую задачу, а юзер только свою
        if (author.getRole() == Role.USER &&
                (task.getExecutor() == null || !task.getExecutor().getId().equals(authorId))) {
            throw new UnauthorizedActionException("Вы можете комментировать только свои задачи!");
        }

        Comment comment = Comment.builder()
                .task(task)
                .author(author)
                .content(content)
                .build();

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskId(taskId);
    }
}