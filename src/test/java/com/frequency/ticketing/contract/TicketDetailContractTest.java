package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TicketDetailContractTest extends AbstractIntegrationTest {

  @Test
  void getExistingTicketReturns200WithEmptyComments() {
    TicketResponse created =
        restTemplate
            .postForEntity(
                baseUrl("/tickets"),
                new TicketCreateRequest("Detail me", "desc", TicketPriority.LOW, null),
                TicketResponse.class)
            .getBody();

    ResponseEntity<TicketDetailResponse> response =
        restTemplate.getForEntity(baseUrl("/tickets/" + created.id()), TicketDetailResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(created.id());
    assertThat(response.getBody().comments()).isEmpty();
  }

  @Test
  void getUnknownTicketReturns404NotFound() {
    ResponseEntity<ApiError> response =
        restTemplate.getForEntity(baseUrl("/tickets/" + UUID.randomUUID()), ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.TICKET_NOT_FOUND.name());
  }
}
