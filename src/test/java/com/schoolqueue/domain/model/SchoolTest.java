package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchoolTest {

  @Test
  @DisplayName("exposes id and name when constructed with an explicit id")
  void shouldExposeIdAndNameWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();

    School school = new School(id, "Escola Municipal");

    assertThat(school.id()).isEqualTo(id);
    assertThat(school.name()).isEqualTo("Escola Municipal");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    School school = new School(null, "Escola Municipal");

    assertThat(school.id()).isNotNull();
    assertThat(school.name()).isEqualTo("Escola Municipal");
  }
}
