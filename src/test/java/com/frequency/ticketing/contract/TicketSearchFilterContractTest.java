package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketPage;
import com.frequency.ticketing.web.dto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TicketSearchFilterContractTest extends AbstractIntegrationTest {

  @Test
  void keywordQueryReturns200WithPageShape() {
    restTemplate.postForEntity(
        baseUrl("/tickets"),
        new TicketCreateRequest("Contract keyword ticket", "desc", TicketPriority.LOW, null),
        TicketResponse.class);

    ResponseEntity<TicketPage> response =
        restTemplate.getForEntity(baseUrl("/tickets?q=Contract%20keyword"), TicketPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().content()).isNotEmpty();
  }

  @Test
  void statusQueryReturns200WithPageShape() {
    ResponseEntity<TicketPage> response =
        restTemplate.getForEntity(baseUrl("/tickets?status=" + TicketStatus.OPEN), TicketPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void combinedQueryAndStatusReturns200() {
    ResponseEntity<TicketPage> response =
        restTemplate.getForEntity(
            baseUrl("/tickets?q=contract&status=" + TicketStatus.OPEN), TicketPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
