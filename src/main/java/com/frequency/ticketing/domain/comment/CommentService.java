package com.frequency.ticketing.domain.comment;

import com.frequency.ticketing.domain.exception.TicketNotFoundException;
import com.frequency.ticketing.repository.CommentRepository;
import com.frequency.ticketing.repository.TicketRepository;
import com.frequency.ticketing.web.dto.CommentMapper;
import com.frequency.ticketing.web.dto.CommentPage;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns comment creation and retrieval. One local {@code @Transactional} unit of work per
 * operation (constitution Design Patterns: Atomicity — no cross-service call here, so SAGA does
 * not apply).
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

  /**
   * Standalone, paginated comment listing for a ticket (feature 002-list-comments, FR-001,
   * FR-005). Rejects with {@link TicketNotFoundException} if the ticket does not exist (FR-004).
   */
  @Transactional(readOnly = true)
  public CommentPage listByTicket(UUID ticketId, Pageable pageable) {
    if (!ticketRepository.existsById(ticketId)) {
      throw new TicketNotFoundException(ticketId);
    }
    return CommentPage.from(
        commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId, pageable).map(CommentMapper::toResponse));
  }
}
