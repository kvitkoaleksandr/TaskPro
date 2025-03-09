package com.example.taskPro.service.interfaces;

import com.example.taskPro.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface TaskServiceInterface {
    Task getTaskById(Long id);

    Task createTask(Task task, Authentication authentication);

    Task updateTask(Long id, Task updatedTask, Authentication authentication);

    void deleteTask(Long id, Authentication authentication);

    Task assignExecutor(Long taskId, Long executorId, Authentication authentication);

    Task updateTaskStatus(Long taskId, String status, Authentication authentication);

    Task updateTaskPriority(Long taskId, String priority, Authentication authentication);

    Task addComment(Long taskId, String comment, Authentication authentication);

    Page<Task> getTasksFiltered(Long authorId, Long executorId, int page, int size);

    Page<Task> getTasksByAuthor(Long authorId, int page, int size);

    Page<Task> getTasksByExecutor(Long executorId, int page, int size);
}