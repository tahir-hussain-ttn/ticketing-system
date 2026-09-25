package com.frequency.ticketing.web.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(UUID id, UUID ticketId, String content, Instant createdAt) {}
