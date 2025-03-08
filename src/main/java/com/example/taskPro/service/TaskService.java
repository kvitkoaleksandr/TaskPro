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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + id + " не найдена"));
    }

    @Transactional
    public Task createTask(Task task, Long adminId) {
        User admin = new User();
        admin.setId(adminId);
        task.setAuthor(admin);

        if (task.getExecutor() != null) {
            User executor = userRepository.findById(task.getExecutor().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Исполнитель с ID " + task.getExecutor().getId() + " не найден"));
            task.setExecutor(executor);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask, Long adminId) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(updatedTask.getTitle());
                    existingTask.setDescription(updatedTask.getDescription());
                    existingTask.setStatus(updatedTask.getStatus());
                    existingTask.setPriority(updatedTask.getPriority());

                    if (updatedTask.getExecutor() != null &&
                            !updatedTask.getExecutor().getId().equals(existingTask.getExecutor() != null
                                    ? existingTask.getExecutor().getId() : null)) {
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
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public Task assignExecutor(Long taskId, Long executorId, Long adminId) {
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
                        throw new RuntimeException("Вы не являетесь исполнителем этой задачи");
                    }
                    task.setStatus(TaskStatus.valueOf(status));
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена"));
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
}