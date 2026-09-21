package com.frequency.ticketing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketPage;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Blank title, blank description, and an invalid priority value are each rejected with 400
 * VALIDATION_FAILED and per-field detail, with no row written (FR-011, SC-005).
 */
class TicketValidationIntegrationTest extends AbstractIntegrationTest {

  @Test
  void blankTitleRejectedWithFieldError() {
    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest("", "desc", TicketPriority.LOW, null),
            ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().code()).isEqualTo(ApiError.Code.VALIDATION_FAILED.name());
    assertThat(response.getBody().fieldErrors()).anySatisfy(fe -> assertThat(fe.field()).isEqualTo("title"));
  }

  @Test
  void blankDescriptionRejectedWithFieldError() {
    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest("Title", "", TicketPriority.LOW, null),
            ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().fieldErrors())
        .anySatisfy(fe -> assertThat(fe.field()).isEqualTo("description"));
  }

  @Test
  void invalidPriorityRejectedAndNoRowWritten() {
    long countBefore = countTickets();

    ResponseEntity<ApiError> response =
        restTemplate.postForEntity(
            baseUrl("/tickets"),
            new HttpEntity<>(Map.of("title", "Bad priority", "description", "d", "priority", "URGENT")),
            ApiError.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(countTickets()).isEqualTo(countBefore);
  }

  private long countTickets() {
    return restTemplate
        .exchange(baseUrl("/tickets?size=1"), HttpMethod.GET, null, TicketPage.class)
        .getBody()
        .totalElements();
  }
}
