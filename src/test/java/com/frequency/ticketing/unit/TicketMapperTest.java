package com.frequency.ticketing.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketMapper;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketMapperTest {

  @Test
  void toResponseMapsEveryField() {
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.HIGH, "jane.doe");

    TicketResponse response = TicketMapper.toResponse(ticket);

    assertThat(response.id()).isEqualTo(ticket.getId());
    assertThat(response.title()).isEqualTo("Title");
    assertThat(response.description()).isEqualTo("Description");
    assertThat(response.priority()).isEqualTo(TicketPriority.HIGH);
    assertThat(response.status()).isEqualTo(ticket.getStatus());
    assertThat(response.assignee()).isEqualTo("jane.doe");
  }

  @Test
  void toDetailResponseIncludesMappedComments() {
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.LOW, null);
    Comment comment = new Comment(ticket.getId(), "a comment");

    TicketDetailResponse detail = TicketMapper.toDetailResponse(ticket, List.of(comment));

    assertThat(detail.id()).isEqualTo(ticket.getId());
    assertThat(detail.comments()).hasSize(1);
    assertThat(detail.comments().get(0).content()).isEqualTo("a comment");
    assertThat(detail.comments().get(0).ticketId()).isEqualTo(ticket.getId());
  }

  @Test
  void toDetailResponseWithNoCommentsIsEmptyNotNull() {
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.LOW, null);

    TicketDetailResponse detail = TicketMapper.toDetailResponse(ticket, List.of());

    assertThat(detail.comments()).isNotNull().isEmpty();
  }
}
