package com.example.taskPro.service;

import com.example.taskPro.exception.EntityNotFoundException;
import com.example.taskPro.model.Task;
import com.example.taskPro.model.TaskStatus;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.TaskRepository;
import com.example.taskPro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository; // Добавили репозиторий пользователей

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + id + " не найдена"));
    }

    public Task createTask(Task task, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

        task.setAuthor(admin);
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task updatedTask, String adminEmail) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(updatedTask.getTitle());
                    existingTask.setDescription(updatedTask.getDescription());
                    existingTask.setStatus(updatedTask.getStatus());
                    existingTask.setPriority(updatedTask.getPriority());
                    existingTask.setExecutor(updatedTask.getExecutor());
                    return taskRepository.save(existingTask);
                })
                .orElseThrow(() -> new RuntimeException("Задача с ID " + id + " не найдена"));
    }

    public void deleteTask(Long id, String adminEmail) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }
        taskRepository.deleteById(id);
    }

    public Task assignExecutor(Long taskId, String executorEmail, String adminEmail) {
        User executor = userRepository.findByEmail(executorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с email " + executorEmail + " не найден"));

        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setExecutor(executor);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена"));
    }

    public Task updateTaskStatus(Long taskId, String status, String userEmail) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    if (!task.getExecutor().getEmail().equals(userEmail)) {
                        throw new RuntimeException("Вы не являетесь исполнителем этой задачи");
                    }
                    try {
                        task.setStatus(TaskStatus.valueOf(status));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Некорректный статус задачи: " + status);
                    }
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена"));
    }


    public Task addComment(Long taskId, String comment, String userEmail) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.getComments().add(userEmail + ": " + comment);
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new RuntimeException("Задача с ID " + taskId + " не найдена"));
    }

    public Page<Task> getTasksFiltered(Long authorId, Long executorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByAuthorIdOrExecutorId(authorId, executorId, pageable);
    }

    public List<Task> getTasksByAuthorOrExecutor(String authorEmail, String executorEmail) {
        if (authorEmail == null && executorEmail == null) {
            throw new IllegalArgumentException("Должен быть указан либо автор, либо исполнитель");
        }

        if (authorEmail != null) {
            return taskRepository.findByAuthorEmail(authorEmail);
        }

        return taskRepository.findByExecutorEmail(executorEmail);
    }
}