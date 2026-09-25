package com.frequency.ticketing.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.domain.ticket.Ticket;
import com.frequency.ticketing.domain.ticket.TicketPriority;
import com.frequency.ticketing.domain.user.User;
import com.frequency.ticketing.domain.user.UserRole;
import com.frequency.ticketing.repository.UserRepository;
import com.frequency.ticketing.web.dto.CommentMapper;
import com.frequency.ticketing.web.dto.TicketDetailResponse;
import com.frequency.ticketing.web.dto.TicketMapper;
import com.frequency.ticketing.web.dto.TicketResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketMapperTest {

  @Mock private UserRepository userRepository;

  @Test
  void toResponseMapsEveryField() {
    UUID createdById = UUID.randomUUID();
    UUID assigneeId = UUID.randomUUID();
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.HIGH, createdById);
    ticket.assignTo(assigneeId);
    when(userRepository.findById(createdById))
        .thenReturn(java.util.Optional.of(new User("Jane Doe", "jane@example.test", "hash", UserRole.GENERAL)));
    when(userRepository.findById(assigneeId))
        .thenReturn(java.util.Optional.of(new User("Sam Support", "sam@example.test", "hash", UserRole.SUPPORT)));

    TicketMapper mapper = new TicketMapper(userRepository, new CommentMapper(userRepository));
    TicketResponse response = mapper.toResponse(ticket);

    assertThat(response.id()).isEqualTo(ticket.getId());
    assertThat(response.title()).isEqualTo("Title");
    assertThat(response.description()).isEqualTo("Description");
    assertThat(response.priority()).isEqualTo(TicketPriority.HIGH);
    assertThat(response.status()).isEqualTo(ticket.getStatus());
    assertThat(response.assignee().name()).isEqualTo("Sam Support");
    assertThat(response.createdBy().name()).isEqualTo("Jane Doe");
  }

  @Test
  void toDetailResponseIncludesMappedComments() {
    UUID createdById = UUID.randomUUID();
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.LOW, createdById);
    Comment comment = new Comment(ticket.getId(), "a comment", createdById);
    when(userRepository.findById(createdById))
        .thenReturn(java.util.Optional.of(new User("Jane Doe", "jane@example.test", "hash", UserRole.GENERAL)));

    TicketMapper mapper = new TicketMapper(userRepository, new CommentMapper(userRepository));
    TicketDetailResponse detail = mapper.toDetailResponse(ticket, List.of(comment));

    assertThat(detail.id()).isEqualTo(ticket.getId());
    assertThat(detail.comments()).hasSize(1);
    assertThat(detail.comments().get(0).content()).isEqualTo("a comment");
    assertThat(detail.comments().get(0).ticketId()).isEqualTo(ticket.getId());
  }

  @Test
  void toDetailResponseWithNoCommentsIsEmptyNotNull() {
    UUID createdById = UUID.randomUUID();
    Ticket ticket = new Ticket("Title", "Description", TicketPriority.LOW, createdById);
    when(userRepository.findById(createdById))
        .thenReturn(java.util.Optional.of(new User("Jane Doe", "jane@example.test", "hash", UserRole.GENERAL)));

    TicketMapper mapper = new TicketMapper(userRepository, new CommentMapper(userRepository));
    TicketDetailResponse detail = mapper.toDetailResponse(ticket, List.of());

    assertThat(detail.comments()).isNotNull().isEmpty();
  }
}
