package com.frequency.ticketing.domain.ticket;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.exception.TicketNotFoundException;
import com.frequency.ticketing.repository.CommentRepository;
import com.frequency.ticketing.repository.TicketRepository;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketMapper;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns every ticket read/write. {@link #applyTransition} is the ONLY method that changes a
 * ticket's status (FR-013) — it delegates to {@link TicketStatusTransitionPolicy}, the single
 * domain component that decides transition legality (constitution Principle II). Every method
 * is one local {@code @Transactional} unit of work (constitution Design Patterns: Atomicity —
 * this feature calls no other service, so SAGA does not apply).
 */
@Service
public class TicketService {

  private static final Logger log = LoggerFactory.getLogger(TicketService.class);

  private final TicketRepository ticketRepository;
  private final CommentRepository commentRepository;
  private final TicketStatusTransitionPolicy transitionPolicy;

  public TicketService(
      TicketRepository ticketRepository,
      CommentRepository commentRepository,
      TicketStatusTransitionPolicy transitionPolicy) {
    this.ticketRepository = ticketRepository;
    this.commentRepository = commentRepository;
    this.transitionPolicy = transitionPolicy;
  }

  @Transactional
  public Ticket create(String title, String description, TicketPriority priority, String assignee) {
    TicketPriority effectivePriority = priority != null ? priority : TicketPriority.MEDIUM;
    Ticket ticket = new Ticket(title, description, effectivePriority, assignee);
    return ticketRepository.save(ticket);
  }

  @Transactional(readOnly = true)
  public Ticket getById(UUID id) {
    return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public TicketDetailResponse getDetailById(UUID id) {
    Ticket ticket = getById(id);
    List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(id);
    return TicketMapper.toDetailResponse(ticket, comments);
  }

  @Transactional(readOnly = true)
  public Page<TicketResponse> list(Pageable pageable) {
    return search(null, null, pageable);
  }

  @Transactional(readOnly = true)
  public Page<TicketResponse> search(String keyword, TicketStatus status, Pageable pageable) {
    return ticketRepository.search(keyword, status, pageable).map(TicketMapper::toResponse);
  }

  @Transactional
  public Ticket update(
      UUID id, String title, String description, TicketPriority priority, String assignee) {
    Ticket ticket = getById(id);
    ticket.updateFields(title, description, priority, assignee);
    return ticket;
  }

  @Transactional
  public Ticket applyTransition(UUID id, TicketStatus newStatus) {
    Ticket ticket = getById(id);
    TicketStatus previous = ticket.getStatus();
    transitionPolicy.transition(ticket, newStatus);
    log.info(
        "ticket_transition ticketId={} from={} to={}", ticket.getId(), previous, ticket.getStatus());
    return ticket;
  }
}
