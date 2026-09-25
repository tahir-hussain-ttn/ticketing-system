package com.frequency.ticketing.web.dto;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Resolves each comment's {@code authorName} from its {@code authorId} (spec 005 FR-015). A
 * Spring-managed bean (not a static utility) because it now needs {@link UserRepository}.
 */
@Component
public class CommentMapper {

  private final UserRepository userRepository;

  public CommentMapper(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public CommentResponse toResponse(Comment comment) {
    String authorName =
        userRepository
            .findById(comment.getAuthorId())
            .map(user -> user.getName())
            .orElse(null);
    return new CommentResponse(
        comment.getId(), comment.getTicketId(), comment.getContent(), authorName,
        comment.getCreatedAt());
  }

  public List<CommentResponse> toResponseList(List<Comment> comments) {
    return comments.stream().map(this::toResponse).toList();
  }
}
