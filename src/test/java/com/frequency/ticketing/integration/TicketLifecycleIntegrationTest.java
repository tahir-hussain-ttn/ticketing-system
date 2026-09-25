package com.frequency.ticketing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketPage;
import com.frequency.ticketing.web.dto.TicketResponse;
import com.frequency.ticketing.web.dto.TicketUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/** Create -> list -> view -> update happy path (SC-001): fields intact on every read. */
class TicketLifecycleIntegrationTest extends AbstractIntegrationTest {

  @Test
  void createListViewUpdateRoundTrip() {
    TicketCreateRequest createRequest =
        new TicketCreateRequest("Lifecycle ticket", "Full round trip", TicketPriority.HIGH, "jane.doe");
    TicketResponse created =
        restTemplate.postForEntity(baseUrl("/tickets"), createRequest, TicketResponse.class).getBody();

    assertThat(created.title()).isEqualTo("Lifecycle ticket");
    assertThat(created.description()).isEqualTo("Full round trip");
    assertThat(created.priority()).isEqualTo(TicketPriority.HIGH);
    assertThat(created.assignee()).isEqualTo("jane.doe");
    assertThat(created.status()).isEqualTo(TicketStatus.OPEN);

    TicketPage page = restTemplate.getForEntity(baseUrl("/tickets"), TicketPage.class).getBody();
    assertThat(page.content()).anySatisfy(t -> assertThat(t.id()).isEqualTo(created.id()));

    TicketDetailResponse detail =
        restTemplate.getForEntity(baseUrl("/tickets/" + created.id()), TicketDetailResponse.class).getBody();
    assertThat(detail.title()).isEqualTo("Lifecycle ticket");
    assertThat(detail.comments()).isEmpty();

    TicketResponse updated =
        restTemplate
            .exchange(
                baseUrl("/tickets/" + created.id()),
                HttpMethod.PATCH,
                new HttpEntity<>(new TicketUpdateRequest("Updated title", null, TicketPriority.CRITICAL, null)),
                TicketResponse.class)
            .getBody();
    assertThat(updated.title()).isEqualTo("Updated title");
    assertThat(updated.description()).isEqualTo("Full round trip"); // untouched field intact
    assertThat(updated.priority()).isEqualTo(TicketPriority.CRITICAL);
    assertThat(updated.assignee()).isEqualTo("jane.doe"); // untouched field intact

    TicketDetailResponse afterUpdate =
        restTemplate.getForEntity(baseUrl("/tickets/" + created.id()), TicketDetailResponse.class).getBody();
    assertThat(afterUpdate.title()).isEqualTo("Updated title");
  }
}
