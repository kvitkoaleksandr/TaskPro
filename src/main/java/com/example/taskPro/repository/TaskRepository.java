package com.example.taskPro.repository;

import com.example.taskPro.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAuthorIdOrExecutorId(Long authorId, Long executorId, Pageable pageable);

    Page<Task> findByAuthorId(Long authorId, Pageable pageable);

    Page<Task> findByExecutorId(Long executorId, Pageable pageable);

}