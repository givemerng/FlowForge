package com.flowforge.service;

import com.flowforge.dto.request.CommentRequest;
import com.flowforge.dto.response.CommentResponse;
import com.flowforge.entity.Comment;
import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.CommentRepository;
import com.flowforge.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserService currentUserService;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                          ProjectAccessService projectAccessService, CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectAccessService = projectAccessService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long taskId) {
        Task task = requireTask(taskId);
        projectAccessService.requireAccess(task.getProject().getId(), currentUserService.requireCurrentUser());
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request) {
        Task task = requireTask(taskId);
        User author = currentUserService.requireCurrentUser();
        projectAccessService.requireAccess(task.getProject().getId(), author);

        Comment comment = new Comment();
        comment.setBody(request.body());
        comment.setTask(task);
        comment.setAuthor(author);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Comment not found");
        }

        User user = currentUserService.requireCurrentUser();
        projectAccessService.requireAccess(comment.getTask().getProject().getId(), user);
        if (!comment.getAuthor().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only the author can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }
}
