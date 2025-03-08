package com.example.taskPro.service;

import com.example.taskPro.exception.EntityNotFoundException;
import com.example.taskPro.exception.TaskNotFoundException;
import com.example.taskPro.exception.UnauthorizedActionException;
import com.example.taskPro.model.Role;
import com.example.taskPro.model.Task;
import com.example.taskPro.model.TaskStatus;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.TaskRepository;
import com.example.taskPro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + id + " не найдена"));
    }

    @Transactional
    public Task createTask(Task task, Long adminId) {
        User admin = validateAdmin(adminId);
        task.setAuthor(admin);

        if (task.getExecutor() != null) {
            User executor = userRepository.findById(task.getExecutor().getId())
                    .orElseThrow(() -> {
                        log.warn("Попытка назначения несуществующего исполнителя: {}", task.getExecutor().getId());
                        return new EntityNotFoundException("Исполнитель с ID " + task.getExecutor()
                                                                                     .getId() + " не найден");
                    });
            task.setExecutor(executor);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask, Long adminId) {
        User admin = validateAdmin(adminId);

        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(updatedTask.getTitle());
                    existingTask.setDescription(updatedTask.getDescription());
                    existingTask.setStatus(updatedTask.getStatus());
                    existingTask.setPriority(updatedTask.getPriority());

                    if (updatedTask.getExecutor() != null) {
                        User executor = userRepository.findById(updatedTask.getExecutor().getId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Исполнитель с ID " + updatedTask.getExecutor().getId() + " не найден"));
                        existingTask.setExecutor(executor);
                    }

                    return taskRepository.save(existingTask);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + id + " не найдена"));
    }

    @Transactional
    public void deleteTask(Long id, Long adminId) {
        User admin = validateAdmin(adminId);

        if (!taskRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующей задачи ID {}", id);
            throw new TaskNotFoundException("Задача с ID " + id + " не найдена");
        }

        taskRepository.deleteById(id);
    }

    @Transactional
    public Task assignExecutor(Long taskId, Long executorId, Long adminId) {
        User admin = validateAdmin(adminId);
        User executor = userRepository.findById(executorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + executorId + " не найден"));

        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setExecutor(executor);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена"));
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, String status, Long userId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    if (task.getExecutor() == null || !task.getExecutor().getId().equals(userId)) {
                        throw new UnauthorizedActionException("Вы не являетесь исполнителем этой задачи!");
                    }
                    task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new TaskNotFoundException ("Задача с ID " + taskId + " не найдена"));
    }

    @Transactional
    public Task addComment(Long taskId, String comment, Long userId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.getComments().add("User " + userId + ": " + comment);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена"));
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
            throw new IllegalArgumentException("Необходимо передать authorId или executorId");
        }
    }

    private User validateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Администратор с ID " + adminId + " не найден"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Только администратор может выполнять это действие!");
        }

        return admin;
    }
}