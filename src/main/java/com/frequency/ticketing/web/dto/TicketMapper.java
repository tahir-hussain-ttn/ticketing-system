package com.frequency.ticketing.web.dto;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code assignee}/{@code createdBy} from their ids (spec 005). A Spring-managed bean
 * (not a static utility) because it now needs {@link UserRepository}.
 */
@Component
public class TicketMapper {

  private final UserRepository userRepository;
  private final CommentMapper commentMapper;

  public TicketMapper(UserRepository userRepository, CommentMapper commentMapper) {
    this.userRepository = userRepository;
    this.commentMapper = commentMapper;
  }

  private UserSummary summaryOf(UUID userId) {
    if (userId == null) {
      return null;
    }
    return userRepository
        .findById(userId)
        .map(user -> new UserSummary(user.getId(), user.getName()))
        .orElse(null);
  }

  public TicketResponse toResponse(Ticket ticket) {
    return new TicketResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        summaryOf(ticket.getAssigneeId()),
        summaryOf(ticket.getCreatedById()),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt());
  }

  public TicketDetailResponse toDetailResponse(Ticket ticket, List<Comment> comments) {
    List<CommentResponse> commentResponses = commentMapper.toResponseList(comments);
    return new TicketDetailResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        summaryOf(ticket.getAssigneeId()),
        summaryOf(ticket.getCreatedById()),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt(),
        commentResponses);
  }
}
