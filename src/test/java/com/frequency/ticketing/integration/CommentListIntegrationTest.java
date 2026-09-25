package com.frequency.ticketing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.CommentCreateRequest;
import com.frequency.ticketing.web.dto.CommentPage;
import com.frequency.ticketing.web.dto.CommentResponse;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Ordering + timestamps (FR-002, FR-002a), empty-list-not-error (FR-003), and pagination
 * (FR-005) for the standalone comment-list endpoint.
 */
class CommentListIntegrationTest extends AbstractIntegrationTest {

  private UUID createTicket(String title) {
    return restTemplate
        .postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest(title, "desc", TicketPriority.LOW, null),
            TicketResponse.class)
        .getBody()
        .id();
  }

  private void addComment(UUID ticketId, String content) {
    restTemplate.postForEntity(
        baseUrl("/tickets/" + ticketId + "/comments"),
        new CommentCreateRequest(content),
        CommentResponse.class);
  }

  @Test
  void threeCommentsListedOldestToNewestWithContentAndTimestamp() {
    UUID ticketId = createTicket("Ordering test");
    addComment(ticketId, "first");
    addComment(ticketId, "second");
    addComment(ticketId, "third");

    CommentPage page =
        restTemplate.getForEntity(baseUrl("/tickets/" + ticketId + "/comments"), CommentPage.class).getBody();

    assertThat(page.content()).extracting(CommentResponse::content).containsExactly("first", "second", "third");
    page.content().forEach(c -> assertThat(c.createdAt()).isNotNull());
  }

  @Test
  void ticketWithNoCommentsReturnsEmptyListNotError() {
    UUID ticketId = createTicket("No comments");

    CommentPage page =
        restTemplate.getForEntity(baseUrl("/tickets/" + ticketId + "/comments"), CommentPage.class).getBody();

    assertThat(page.content()).isEmpty();
    assertThat(page.totalElements()).isZero();
  }

  @Test
  void pagesThroughCommentsCorrectly() {
    UUID ticketId = createTicket("Pagination test");
    addComment(ticketId, "c1");
    addComment(ticketId, "c2");
    addComment(ticketId, "c3");

    CommentPage firstPage =
        restTemplate
            .getForEntity(baseUrl("/tickets/" + ticketId + "/comments?page=0&size=2"), CommentPage.class)
            .getBody();
    CommentPage secondPage =
        restTemplate
            .getForEntity(baseUrl("/tickets/" + ticketId + "/comments?page=1&size=2"), CommentPage.class)
            .getBody();

    assertThat(firstPage.content()).hasSize(2);
    assertThat(firstPage.totalElements()).isEqualTo(3);
    assertThat(firstPage.totalPages()).isEqualTo(2);
    assertThat(secondPage.content()).hasSize(1);
    assertThat(secondPage.content().get(0).content()).isEqualTo("c3");
  }
}
