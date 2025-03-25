package com.example.taskpro.dto;

import com.example.taskpro.entity.TaskPriority;
import com.example.taskpro.entity.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {

    @NotNull(message = "Task ID is required")
    private Long id;

    @NotEmpty(message = "Title is required")
    private String title;

    private String description;

    private TaskStatus status = TaskStatus.PENDING;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    private Long executorId;
}