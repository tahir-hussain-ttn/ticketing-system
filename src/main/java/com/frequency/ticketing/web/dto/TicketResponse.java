package com.frequency.ticketing.web.dto;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String title,
    String description,
    TicketPriority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt,
    Instant updatedAt) {}
