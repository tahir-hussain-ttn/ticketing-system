package com.frequency.ticketing.repository;

import com.frequency.ticketing.domain.comment.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  List<Comment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
