package com.frequency.ticketing.repository;

import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  /**
   * Keyword search (case-insensitive substring on title/description, backed by the pg_trgm GIN
   * indexes from V1__init_schema.sql) combined with an optional exact status filter. Either
   * {@code keyword} or {@code status} may be {@code null} to skip that condition (FR-006, FR-007).
   * A native query is used (rather than HQL {@code ilike}) so PostgreSQL's own {@code ILIKE}
   * operator is what actually executes, guaranteeing the pg_trgm indexes apply.
   */
  @Query(
      value =
          "SELECT * FROM tickets t WHERE "
              + "(CAST(:keyword AS varchar) IS NULL OR t.title ILIKE CONCAT('%', CAST(:keyword AS varchar), '%') "
              + "OR t.description ILIKE CONCAT('%', CAST(:keyword AS varchar), '%')) "
              + "AND (CAST(:status AS varchar) IS NULL OR t.status = CAST(:status AS varchar))",
      countQuery =
          "SELECT count(*) FROM tickets t WHERE "
              + "(CAST(:keyword AS varchar) IS NULL OR t.title ILIKE CONCAT('%', CAST(:keyword AS varchar), '%') "
              + "OR t.description ILIKE CONCAT('%', CAST(:keyword AS varchar), '%')) "
              + "AND (CAST(:status AS varchar) IS NULL OR t.status = CAST(:status AS varchar))",
      nativeQuery = true)
  Page<Ticket> searchByStatusName(
      @Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

  default Page<Ticket> search(String keyword, TicketStatus status, Pageable pageable) {
    return searchByStatusName(keyword, status == null ? null : status.name(), pageable);
  }
}
