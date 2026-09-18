package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParentTest {

  @Test
  @DisplayName("exposes id, name, phone and email when constructed with an explicit id")
  void shouldExposeIdNamePhoneAndEmailWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();

    Parent parent = new Parent(id, "Maria Souza", "11999998888", "maria@mail.com");

    assertThat(parent.id()).isEqualTo(id);
    assertThat(parent.name()).isEqualTo("Maria Souza");
    assertThat(parent.phone()).isEqualTo("11999998888");
    assertThat(parent.email()).isEqualTo("maria@mail.com");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    Parent parent = new Parent(null, "Maria Souza", "11999998888", "maria@mail.com");

    assertThat(parent.id()).isNotNull();
    assertThat(parent.name()).isEqualTo("Maria Souza");
    assertThat(parent.phone()).isEqualTo("11999998888");
    assertThat(parent.email()).isEqualTo("maria@mail.com");
  }

  @Test
  @DisplayName("changes name, phone and email when updated via setters")
  void shouldChangeNamePhoneAndEmailWhenUpdatedViaSetters() {
    Parent parent = new Parent(UUID.randomUUID(), "Maria Souza", "11999998888", "maria@mail.com");

    parent.setName("Maria Souza Silva");
    parent.setPhone("11888887777");
    parent.setEmail("maria.silva@mail.com");

    assertThat(parent.name()).isEqualTo("Maria Souza Silva");
    assertThat(parent.phone()).isEqualTo("11888887777");
    assertThat(parent.email()).isEqualTo("maria.silva@mail.com");
  }
}
