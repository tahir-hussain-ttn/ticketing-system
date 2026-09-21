package com.frequency.ticketing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.ticket.TicketStatus;
import com.frequency.ticketing.support.AbstractIntegrationTest;
import com.frequency.ticketing.web.dto.TicketCreateRequest;
import com.frequency.ticketing.web.dto.TicketPage;
import com.frequency.ticketing.web.dto.TicketResponse;
import com.frequency.ticketing.web.dto.TicketTransitionRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Keyword match, empty-result (not error), status filter, and combined filter (FR-006, FR-007). */
class TicketSearchFilterIntegrationTest extends AbstractIntegrationTest {

  private UUID create(String title, String description) {
    return restTemplate
        .postForEntity(
            baseUrl("/tickets"),
            new TicketCreateRequest(title, description, TicketPriority.MEDIUM, null),
            TicketResponse.class)
        .getBody()
        .id();
  }

  @Test
  void keywordMatchesTitleOrDescription() {
    String unique = "zx" + UUID.randomUUID().toString().substring(0, 8);
    create(unique + " printer issue", "unrelated description");
    create("unrelated title", "mentions " + unique + " somewhere");
    create("completely unrelated", "nothing matches here");

    TicketPage page =
        restTemplate.getForEntity(baseUrl("/tickets?q=" + unique), TicketPage.class).getBody();

    assertThat(page.content()).hasSize(2);
  }

  @Test
  void nonMatchingKeywordReturnsEmptyListNotError() {
    String noMatch = "zzzznomatch" + UUID.randomUUID();

    TicketPage page =
        restTemplate.getForEntity(baseUrl("/tickets?q=" + noMatch), TicketPage.class).getBody();

    assertThat(page.content()).isEmpty();
    assertThat(page.totalElements()).isZero();
  }

  @Test
  void statusFilterReturnsOnlyMatchingStatus() {
    String marker = "statusfilter" + UUID.randomUUID().toString().substring(0, 8);
    UUID openId = create(marker + " open ticket", "d");
    UUID cancelledId = create(marker + " cancelled ticket", "d");
    restTemplate.postForEntity(
        baseUrl("/tickets/" + cancelledId + "/transitions"),
        new TicketTransitionRequest(TicketStatus.CANCELLED),
        TicketResponse.class);

    TicketPage cancelledPage =
        restTemplate
            .getForEntity(baseUrl("/tickets?q=" + marker + "&status=CANCELLED"), TicketPage.class)
            .getBody();
    TicketPage openPage =
        restTemplate
            .getForEntity(baseUrl("/tickets?q=" + marker + "&status=OPEN"), TicketPage.class)
            .getBody();

    assertThat(cancelledPage.content()).extracting(TicketResponse::id).containsExactly(cancelledId);
    assertThat(openPage.content()).extracting(TicketResponse::id).containsExactly(openId);
  }
}
