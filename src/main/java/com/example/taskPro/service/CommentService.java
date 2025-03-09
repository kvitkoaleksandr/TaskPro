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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment addComment(Long taskId, Long authorId, String content) {
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