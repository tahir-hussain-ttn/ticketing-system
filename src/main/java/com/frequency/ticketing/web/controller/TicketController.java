package com.frequency.ticketing.web.controller;

import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.domain.ticket.TicketService;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketMapper;
import com.frequency.ticketing.web.dto.TicketPage;
import com.frequency.ticketing.web.dto.TicketResponse;
import com.frequency.ticketing.web.dto.TicketTransitionRequest;
import com.frequency.ticketing.web.dto.TicketUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Matches contracts/tickets-api.yaml exactly. Constructor injection only. Every endpoint is
 * annotated so the generated OpenAPI description (constitution Principle I) carries real
 * summaries and response shapes, not just the bare route table springdoc would infer on its own.
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Ticket lifecycle: create, view, update, and status transitions")
public class TicketController {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @PostMapping
  @Operation(summary = "Create a ticket", description = "Creates a ticket with status OPEN (FR-001).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Ticket created",
        content = @Content(schema = @Schema(implementation = TicketResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
    Ticket ticket =
        ticketService.create(
            request.title(), request.description(), request.priority(), request.assignee());
    return ResponseEntity.status(HttpStatus.CREATED).body(TicketMapper.toResponse(ticket));
  }

  @GetMapping
  @Operation(
      summary = "List tickets",
      description = "Lists tickets, optionally filtered by keyword and/or status (FR-002, FR-006, FR-007).")
  @ApiResponse(
      responseCode = "200",
      description = "Page of tickets matching the given filters",
      content = @Content(schema = @Schema(implementation = TicketPage.class)))
  public TicketPage list(
      @Parameter(description = "Keyword matched against title and description") @RequestParam(required = false)
          String q,
      @Parameter(description = "Filter by exact ticket status") @RequestParam(required = false)
          TicketStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
    int effectiveSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    Pageable pageable = PageRequest.of(Math.max(page, 0), effectiveSize);
    Page<TicketResponse> result = ticketService.search(q, status, pageable);
    return TicketPage.from(result);
  }

  @GetMapping(value = "/{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get ticket details", description = "Full ticket record including comments (FR-003).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = TicketDetailResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Ticket not found",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public TicketDetailResponse getById(@PathVariable UUID ticketId) {
    return ticketService.getDetailById(ticketId);
  }

  @PatchMapping("/{ticketId}")
  @Operation(
      summary = "Update ticket fields",
      description =
          "Updates title/description/priority/assignee (FR-004). Never accepts status — status "
              + "changes only via POST .../transitions (FR-013).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = TicketResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Ticket not found",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Concurrent update conflict",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public TicketResponse update(
      @PathVariable UUID ticketId, @Valid @RequestBody TicketUpdateRequest request) {
    Ticket ticket =
        ticketService.update(
            ticketId, request.title(), request.description(), request.priority(), request.assignee());
    return TicketMapper.toResponse(ticket);
  }

  @PostMapping("/{ticketId}/transitions")
  @Operation(
      summary = "Transition ticket status",
      description = "Applies a status transition per the allowed state machine (FR-009, FR-010).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = TicketResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Ticket not found",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Transition not allowed from the current status, or a concurrent update conflict",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public TicketResponse transition(
      @PathVariable UUID ticketId, @Valid @RequestBody TicketTransitionRequest request) {
    Ticket ticket = ticketService.applyTransition(ticketId, request.status());
    return TicketMapper.toResponse(ticket);
  }
}
