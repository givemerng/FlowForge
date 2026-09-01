package com.flowforge.controller;

import com.flowforge.entity.Comment;
import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.CommentRepository;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long taskId,
                                               @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal UserDetailsImpl principal) {
        Task task = taskRepository.findById(taskId)
                .orElse(null);
        if (task == null) return ResponseEntity.notFound().build();

        User author = userRepository.findById(principal.getId())
                .orElse(null);
        if (author == null) return ResponseEntity.status(401).build();

        Comment comment = new Comment();
        comment.setBody(body.get("body"));
        comment.setTask(task);
        comment.setAuthor(author);
        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long taskId,
                                            @PathVariable Long commentId,
                                            @AuthenticationPrincipal UserDetailsImpl principal) {
        return commentRepository.findById(commentId).map(c -> {
            if (!c.getAuthor().getId().equals(principal.getId())) {
                return ResponseEntity.status(403).build();
            }
            commentRepository.delete(c);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
