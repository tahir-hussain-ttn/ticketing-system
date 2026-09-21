package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketPage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TicketListContractTest extends AbstractIntegrationTest {

  @Test
  void listTicketsReturns200WithPageShape() {
    restTemplate.postForEntity(
        baseUrl("/tickets"),
        new TicketCreateRequest("List me", "desc", TicketPriority.MEDIUM, null),
        Void.class);

    ResponseEntity<TicketPage> response = restTemplate.getForEntity(baseUrl("/tickets"), TicketPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).isNotEmpty();
    assertThat(response.getBody().content().stream().anyMatch(t -> t.title().equals("List me"))).isTrue();
  }
}
