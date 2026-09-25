package com.frequency.ticketing.web.dto;

import com.frequency.ticketing.domain.comment.Comment;
import java.util.List;

public final class CommentMapper {

  private CommentMapper() {}

  public static CommentResponse toResponse(Comment comment) {
    return new CommentResponse(
        comment.getId(), comment.getTicketId(), comment.getContent(), comment.getCreatedAt());
  }

  public static List<CommentResponse> toResponseList(List<Comment> comments) {
    return comments.stream().map(CommentMapper::toResponse).toList();
  }
}
