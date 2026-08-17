package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClassroomTest {

  @Test
  @DisplayName("exposes id, schoolId and name when constructed with an explicit id")
  void shouldExposeIdSchoolIdAndNameWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();

    Classroom classroom = new Classroom(id, schoolId, "Turma A");

    assertThat(classroom.id()).isEqualTo(id);
    assertThat(classroom.schoolId()).isEqualTo(schoolId);
    assertThat(classroom.name()).isEqualTo("Turma A");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    UUID schoolId = UUID.randomUUID();

    Classroom classroom = new Classroom(null, schoolId, "Turma A");

    assertThat(classroom.id()).isNotNull();
    assertThat(classroom.schoolId()).isEqualTo(schoolId);
    assertThat(classroom.name()).isEqualTo("Turma A");
  }

  @Test
  @DisplayName("changes school and name when updated via setters")
  void shouldChangeSchoolAndNameWhenUpdatedViaSetters() {
    Classroom classroom = new Classroom(UUID.randomUUID(), UUID.randomUUID(), "Turma A");
    UUID newSchoolId = UUID.randomUUID();

    classroom.setSchoolId(newSchoolId);
    classroom.setName("Turma B");

    assertThat(classroom.schoolId()).isEqualTo(newSchoolId);
    assertThat(classroom.name()).isEqualTo("Turma B");
  }
}
