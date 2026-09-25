package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.CommentCreateRequest;
import com.frequency.ticketing.web.dto.CommentResponse;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CommentCreateContractTest extends AbstractIntegrationTest {

  private UUID createTicket() {
    return restTemplate
        .postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest("Comment target", "desc", TicketPriority.LOW, null),
            TicketResponse.class)
        .getBody()
        .id();
  }

  @Test
  void addCommentReturns201() {
    UUID ticketId = createTicket();

    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            baseUrl("/tickets/" + ticketId + "/comments"),
            new CommentCreateRequest("Escalated to facilities."),
            CommentResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().ticketId()).isEqualTo(ticketId);
    assertThat(response.getBody().content()).isEqualTo("Escalated to facilities.");
  }

  @Test
  void emptyContentReturns400ValidationFailed() {
    UUID ticketId = createTicket();

    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets/" + ticketId + "/comments"), new CommentCreateRequest(""), ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.VALIDATION_FAILED.name());
  }

  @Test
  void unknownTicketReturns404NotFound() {
    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets/" + UUID.randomUUID() + "/comments"),
            new CommentCreateRequest("orphan comment"),
            ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.TICKET_NOT_FOUND.name());
  }
}
