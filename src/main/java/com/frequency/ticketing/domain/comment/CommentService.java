package com.frequency.ticketing.domain.comment;

import com.frequency.ticketing.domain.exception.TicketNotFoundException;
import com.frequency.ticketing.repository.CommentRepository;
import com.frequency.ticketing.repository.TicketRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns comment creation. One local {@code @Transactional} unit of work per operation
 * (constitution Design Patterns: Atomicity — no cross-service call here, so SAGA does not apply).
 */
@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final TicketRepository ticketRepository;

  public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository) {
    this.commentRepository = commentRepository;
    this.ticketRepository = ticketRepository;
  }

  @Transactional
  public Comment addComment(UUID ticketId, String content) {
    if (!ticketRepository.existsById(ticketId)) {
      throw new TicketNotFoundException(ticketId);
    }
    return commentRepository.save(new Comment(ticketId, content));
  }
}
