package com.frequency.ticketing.web.dto;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.ticket.Ticket;
import java.util.List;

/** Hand-written entity/DTO mapping (research.md: DTO ↔ entity mapping decision). */
public final class TicketMapper {

  private TicketMapper() {}

  public static TicketResponse toResponse(Ticket ticket) {
    return new TicketResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        ticket.getAssignee(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt());
  }

  public static TicketDetailResponse toDetailResponse(Ticket ticket, List<Comment> comments) {
    List<CommentResponse> commentResponses = CommentMapper.toResponseList(comments);
    return new TicketDetailResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        ticket.getAssignee(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt(),
        commentResponses);
  }
}
