package com.flowforge.dto.response;

import com.flowforge.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String body, UserSummaryResponse author, LocalDateTime createdAt) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getBody(),
                UserSummaryResponse.from(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }
}
