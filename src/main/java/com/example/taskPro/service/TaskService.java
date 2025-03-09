package com.example.taskPro.service;

import com.example.taskPro.exception.*;
import com.example.taskPro.model.*;
import com.example.taskPro.repository.TaskRepository;
import com.example.taskPro.repository.UserRepository;
import com.example.taskPro.service.interfaces.TaskServiceInterface;
import com.example.taskPro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService implements TaskServiceInterface {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + id + " не найдена"));
    }

    @Transactional
    public Task createTask(Task task, Authentication authentication) {
        Long adminId = jwtUtil.getEntityIdFromAuth(authentication);
        User admin = validateAdmin(adminId);
        task.setAuthor(admin);

        if (task.getExecutor() != null) {
            User executor = userRepository.findById(task.getExecutor().getId())
                    .orElseThrow(() -> {
                        log.warn("Попытка назначения несуществующего исполнителя: {}", task.getExecutor().getId());
                        return new UserNotFoundException("Исполнитель с ID " + task.getExecutor().getId() + " не найден");
                    });
            task.setExecutor(executor);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask, Authentication authentication) {
        Long adminId = jwtUtil.getEntityIdFromAuth(authentication);
        User admin = validateAdmin(adminId);

        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(updatedTask.getTitle());
                    existingTask.setDescription(updatedTask.getDescription());
                    existingTask.setStatus(updatedTask.getStatus());
                    existingTask.setPriority(updatedTask.getPriority());

                    if (updatedTask.getExecutor() != null) {
                        User executor = userRepository.findById(updatedTask.getExecutor().getId())
                                .orElseThrow(() -> new UserNotFoundException(
                                        "Исполнитель с ID " + updatedTask.getExecutor().getId() + " не найден"));
                        existingTask.setExecutor(executor);
                    }

                    return taskRepository.save(existingTask);
                })
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + id + " не найдена"));
    }

    @Transactional
    public void deleteTask(Long id, Authentication authentication) {
        Long adminId = jwtUtil.getEntityIdFromAuth(authentication);
        User admin = validateAdmin(adminId);

        if (!taskRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующей задачи ID {}", id);
            throw new TaskNotFoundException("Задача с ID " + id + " не найдена");
        }

        taskRepository.deleteById(id);
    }

    @Transactional
    public Task assignExecutor(Long taskId, Long executorId, Authentication authentication) {
        Long adminId = jwtUtil.getEntityIdFromAuth(authentication);
        User admin = validateAdmin(adminId);
        User executor = userRepository.findById(executorId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + executorId + " не найден"));

        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setExecutor(executor);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, String status, Authentication authentication) {
        Long userId = jwtUtil.getEntityIdFromAuth(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));

        if (task.getExecutor() == null || !task.getExecutor().getId().equals(userId)) {
            throw new UnauthorizedActionException("Вы не можете менять статус чужой задачи!");
        }

        try {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidTaskStatusException("Некорректный статус задачи: " + status);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskPriority(Long taskId, String priority, Authentication authentication) {
        Long adminId = jwtUtil.getEntityIdFromAuth(authentication);
        validateAdmin(adminId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));

        try {
            task.setPriority(TaskPriority.valueOf(priority.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidTaskPriorityException("Некорректный приоритет задачи: " + priority);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task addComment(Long taskId, String comment, Authentication authentication) {
        Long userId = jwtUtil.getEntityIdFromAuth(authentication);

        return taskRepository.findById(taskId)
                .map(task -> {
                    task.getComments().add("User " + userId + ": " + comment);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));
    }

    public Page<Task> getTasksFiltered(Long authorId, Long executorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (authorId != null && executorId != null) {
            return taskRepository.findByAuthorIdOrExecutorId(authorId, executorId, pageable);
        } else if (authorId != null) {
            return taskRepository.findByAuthorId(authorId, pageable);
        } else if (executorId != null) {
            return taskRepository.findByExecutorId(executorId, pageable);
        } else {
            throw new IllegalArgumentException("Должен быть указан либо authorId, "
                    + "либо executorId, иначе фильтрация невозможна.");
        }
    }

    private User validateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Администратор с ID " + adminId + " не найден"));

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException("Только администратор может выполнять это действие!");
        }

        return admin;
    }

    public Page<Task> getTasksByAuthor(Long authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByAuthorId(authorId, pageable);
    }

    public Page<Task> getTasksByExecutor(Long executorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByExecutorId(executorId, pageable);
    }
}