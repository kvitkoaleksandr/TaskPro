package com.example.taskpro.repository;

import com.example.taskpro.entity.Task;
import com.example.taskpro.entity.TaskStatus;
import com.example.taskpro.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAuthorId(Long authorId);

    List<Task> findByExecutorId(Long executorId);

    List<Task> findByExecutorIdAndStatus(Long executorId, TaskStatus status);

    List<Task> findByAuthorIdAndPriority(Long authorId, TaskPriority priority);
}