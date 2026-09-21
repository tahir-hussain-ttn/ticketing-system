package com.frequency.ticketing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketResponse;
import com.frequency.ticketing.web.dto.TicketTransitionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TicketTransitionContractTest extends AbstractIntegrationTest {

  @Test
  void legalTransitionReturns200() {
    TicketResponse created =
        restTemplate
            .postForEntity(
                baseUrl("/tickets"),
                new TicketCreateRequest("Transition me", "desc", TicketPriority.LOW, null),
                TicketResponse.class)
            .getBody();

    ResponseEntity<TicketResponse> response =
        restTemplate.postForEntity(
            baseUrl("/tickets/" + created.id() + "/transitions"),
            new TicketTransitionRequest(TicketStatus.IN_PROGRESS),
            TicketResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(TicketStatus.IN_PROGRESS);
  }

  @Test
  void illegalTransitionReturns409InvalidTransition() {
    TicketResponse created =
        restTemplate
            .postForEntity(
                baseUrl("/tickets"),
                new TicketCreateRequest("Illegal move", "desc", TicketPriority.LOW, null),
                TicketResponse.class)
            .getBody();

    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets/" + created.id() + "/transitions"),
            new TicketTransitionRequest(TicketStatus.CLOSED),
            ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.INVALID_TRANSITION.name());
  }
}
