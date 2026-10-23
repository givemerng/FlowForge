package com.flowforge.controller;

import com.flowforge.dto.request.CommentRequest;
import com.flowforge.dto.response.CommentResponse;
import com.flowforge.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@Tag(name = "Comment", description = "API endpoints for Comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "Get Comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getComments(taskId));
    }

    @PostMapping
    @Operation(summary = "Add Comment")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId,
                                                       @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(taskId, request));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete Comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long taskId,
                                               @PathVariable Long commentId) {
        commentService.deleteComment(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
