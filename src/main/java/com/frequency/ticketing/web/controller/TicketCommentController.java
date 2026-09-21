package com.frequency.ticketing.web.controller;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.comment.CommentService;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.CommentCreateRequest;
import com.frequency.ticketing.web.dto.CommentMapper;
import com.frequency.ticketing.web.dto.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Matches contracts/tickets-api.yaml exactly. Constructor injection only. */
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments", description = "Adding comments to a ticket (FR-005)")
public class TicketCommentController {

  private final CommentService commentService;

  public TicketCommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping
  @Operation(summary = "Add a comment", description = "Adds a timestamped comment to an existing ticket (FR-005).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Comment created",
        content = @Content(schema = @Schema(implementation = CommentResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed (e.g. empty content)",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Ticket not found",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<CommentResponse> addComment(
      @PathVariable UUID ticketId, @Valid @RequestBody CommentCreateRequest request) {
    Comment comment = commentService.addComment(ticketId, request.content());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentMapper.toResponse(comment));
  }
}
