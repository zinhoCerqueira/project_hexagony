package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParentTest {

  @Test
  @DisplayName("exposes id, name and phone when constructed with an explicit id")
  void shouldExposeIdNameAndPhoneWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();

    Parent parent = new Parent(id, "Maria Souza", "11999998888");

    assertThat(parent.id()).isEqualTo(id);
    assertThat(parent.name()).isEqualTo("Maria Souza");
    assertThat(parent.phone()).isEqualTo("11999998888");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    Parent parent = new Parent(null, "Maria Souza", "11999998888");

    assertThat(parent.id()).isNotNull();
    assertThat(parent.name()).isEqualTo("Maria Souza");
    assertThat(parent.phone()).isEqualTo("11999998888");
  }
}
