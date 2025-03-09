package com.example.taskPro.service.interfaces;

import com.example.taskPro.model.Comment;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CommentServiceInterface {
    Comment addComment(Long taskId, String content, Authentication authentication);
    List<Comment> getCommentsByTask(Long taskId);
}