package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.CommentCreateRequest;
import com.frequency.ticketing.web.dto.CommentPage;
import com.frequency.ticketing.web.dto.CommentResponse;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CommentListContractTest extends AbstractIntegrationTest {

  private UUID createTicket() {
    return restTemplate
        .postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest("Comment list contract", "desc", TicketPriority.LOW, null),
            TicketResponse.class)
        .getBody()
        .id();
  }

  @Test
  void listCommentsReturns200WithPageShapeAndTimestamps() {
    UUID ticketId = createTicket();
    restTemplate.postForEntity(
        baseUrl("/tickets/" + ticketId + "/comments"),
        new CommentCreateRequest("first"),
        CommentResponse.class);

    ResponseEntity<CommentPage> response =
        restTemplate.getForEntity(baseUrl("/tickets/" + ticketId + "/comments"), CommentPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    CommentPage body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.content()).hasSize(1);
    assertThat(body.content().get(0).content()).isEqualTo("first");
    assertThat(body.content().get(0).createdAt()).isNotNull();
    assertThat(body.totalElements()).isEqualTo(1);
  }

  @Test
  void listCommentsForUnknownTicketReturns404NotFound() {
    ResponseEntity<ApiError> response =
        restTemplate.getForEntity(
            baseUrl("/tickets/" + UUID.randomUUID() + "/comments"), ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.TICKET_NOT_FOUND.name());
  }
}
