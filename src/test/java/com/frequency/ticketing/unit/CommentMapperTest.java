package com.frequency.ticketing.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.frequency.ticketing.domain.comment.Comment;
import com.frequency.ticketing.web.dto.CommentMapper;
import com.frequency.ticketing.web.dto.CommentResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommentMapperTest {

  @Test
  void toResponseMapsEveryField() {
    UUID ticketId = UUID.randomUUID();
    Comment comment = new Comment(ticketId, "hello world");

    CommentResponse response = CommentMapper.toResponse(comment);

    assertThat(response.id()).isEqualTo(comment.getId());
    assertThat(response.ticketId()).isEqualTo(ticketId);
    assertThat(response.content()).isEqualTo("hello world");
  }

  @Test
  void toResponseListPreservesOrder() {
    UUID ticketId = UUID.randomUUID();
    Comment first = new Comment(ticketId, "first");
    Comment second = new Comment(ticketId, "second");

    List<CommentResponse> responses = CommentMapper.toResponseList(List.of(first, second));

    assertThat(responses).extracting(CommentResponse::content).containsExactly("first", "second");
  }
}
