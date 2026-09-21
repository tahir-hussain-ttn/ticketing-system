package com.frequency.ticketing.domain.ticket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * A unit of support work. {@code status} is mutated ONLY via {@link #applyTransition}, which is
 * package-private so that only {@link TicketStatusTransitionPolicy} (same package) can move a
 * ticket between statuses — per FR-013, direct field updates must never be able to set status.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

  @Id private UUID id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TicketPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TicketStatus status;

  @Column(length = 200)
  private String assignee;

  @Version private long version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Ticket() {
    // JPA
  }

  public Ticket(String title, String description, TicketPriority priority, String assignee) {
    this.id = UUID.randomUUID();
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.assignee = assignee;
    this.status = TicketStatus.OPEN;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /** Package-private: only {@link TicketStatusTransitionPolicy} may call this. */
  void applyTransition(TicketStatus newStatus) {
    this.status = newStatus;
  }

  public void updateFields(
      String title, String description, TicketPriority priority, String assignee) {
    if (title != null) {
      this.title = title;
    }
    if (description != null) {
      this.description = description;
    }
    if (priority != null) {
      this.priority = priority;
    }
    if (assignee != null) {
      this.assignee = assignee;
    }
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TicketPriority getPriority() {
    return priority;
  }

  public TicketStatus getStatus() {
    return status;
  }

  public String getAssignee() {
    return assignee;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
