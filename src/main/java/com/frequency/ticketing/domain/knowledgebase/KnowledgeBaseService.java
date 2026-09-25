package com.frequency.ticketing.domain.knowledgebase;

import com.frequency.ticketing.domain.ai.EmbeddingClient;
import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.domain.ticket.TicketResolvedEvent;
import com.frequency.ticketing.repository.CommentRepository;
import com.frequency.ticketing.repository.KnowledgeBaseEntryRepository;
import com.frequency.ticketing.repository.TicketRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Builds/refreshes a resolved ticket's knowledge base entry after its transition has committed —
 * decoupled from the transition itself so a slow or failing embedding call never affects ticket
 * creation/transition latency or correctness (spec 005 FR-018; research.md "Knowledge base
 * indexing trigger").
 */
@Service
public class KnowledgeBaseService {

  private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

  private final TicketRepository ticketRepository;
  private final CommentRepository commentRepository;
  private final KnowledgeBaseEntryRepository knowledgeBaseEntryRepository;
  private final EmbeddingClient embeddingClient;

  public KnowledgeBaseService(
      TicketRepository ticketRepository,
      CommentRepository commentRepository,
      KnowledgeBaseEntryRepository knowledgeBaseEntryRepository,
      EmbeddingClient embeddingClient) {
    this.ticketRepository = ticketRepository;
    this.commentRepository = commentRepository;
    this.knowledgeBaseEntryRepository = knowledgeBaseEntryRepository;
    this.embeddingClient = embeddingClient;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTicketResolved(TicketResolvedEvent event) {
    indexTicket(event.ticketId());
  }

  @Transactional
  public void indexTicket(java.util.UUID ticketId) {
    Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
    if (ticket == null) {
      log.warn("knowledge_base_index_skipped ticketId={} reason=ticket_not_found", ticketId);
      return;
    }

    List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    String resolutionContent =
        comments.stream().map(Comment::getContent).collect(Collectors.joining("\n"));
    boolean hasResolutionContent = !comments.isEmpty();

    float[] embedding = embeddingClient.embed(ticket.getTitle() + "\n" + ticket.getDescription());

    knowledgeBaseEntryRepository
        .findByTicketId(ticketId)
        .ifPresentOrElse(
            existing -> existing.refresh(embedding, resolutionContent, hasResolutionContent),
            () ->
                knowledgeBaseEntryRepository.save(
                    new KnowledgeBaseEntry(ticketId, embedding, resolutionContent, hasResolutionContent)));

    log.info("knowledge_base_indexed ticketId={} hasResolutionContent={}", ticketId, hasResolutionContent);
  }
}
