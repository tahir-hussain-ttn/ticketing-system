package com.frequency.ticketing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.CommentCreateRequest;
import com.frequency.ticketing.web.dto.CommentResponse;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Comments returned in ascending createdAt order with content intact (FR-005). */
class CommentIntegrationTest extends AbstractIntegrationTest {

  @Test
  void threeCommentsReturnedInOrderOnTicketDetail() {
    UUID ticketId =
        restTemplate
            .postForEntity(
                baseUrl("/tickets"),
                new TicketCreateRequest("Comment history", "desc", TicketPriority.LOW, null),
                TicketResponse.class)
            .getBody()
            .id();

    restTemplate.postForEntity(
        baseUrl("/tickets/" + ticketId + "/comments"),
        new CommentCreateRequest("first"),
        CommentResponse.class);
    restTemplate.postForEntity(
        baseUrl("/tickets/" + ticketId + "/comments"),
        new CommentCreateRequest("second"),
        CommentResponse.class);
    restTemplate.postForEntity(
        baseUrl("/tickets/" + ticketId + "/comments"),
        new CommentCreateRequest("third"),
        CommentResponse.class);

    TicketDetailResponse detail =
        restTemplate.getForEntity(baseUrl("/tickets/" + ticketId), TicketDetailResponse.class).getBody();

    assertThat(detail.comments()).hasSize(3);
    assertThat(detail.comments().stream().map(CommentResponse::content).toList())
        .containsExactly("first", "second", "third");
    detail.comments().forEach(c -> assertThat(c.ticketId()).isEqualTo(ticketId));
  }
}
