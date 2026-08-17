package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudentTest {

  @Test
  @DisplayName("exposes id, schoolId, classroomId and name when constructed with an explicit id")
  void shouldExposeIdSchoolIdClassroomIdAndNameWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();

    Student student = new Student(id, schoolId, classroomId, "João Silva");

    assertThat(student.id()).isEqualTo(id);
    assertThat(student.schoolId()).isEqualTo(schoolId);
    assertThat(student.classroomId()).isEqualTo(classroomId);
    assertThat(student.name()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();

    Student student = new Student(null, schoolId, classroomId, "João Silva");

    assertThat(student.id()).isNotNull();
    assertThat(student.schoolId()).isEqualTo(schoolId);
    assertThat(student.classroomId()).isEqualTo(classroomId);
    assertThat(student.name()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("changes school, classroom and name when updated via setters")
  void shouldChangeSchoolClassroomAndNameWhenUpdatedViaSetters() {
    Student student = new Student(null, UUID.randomUUID(), UUID.randomUUID(), "João Silva");
    UUID newSchoolId = UUID.randomUUID();
    UUID newClassroomId = UUID.randomUUID();

    student.setSchoolId(newSchoolId);
    student.setClassroomId(newClassroomId);
    student.setName("João Souza");

    assertThat(student.schoolId()).isEqualTo(newSchoolId);
    assertThat(student.classroomId()).isEqualTo(newClassroomId);
    assertThat(student.name()).isEqualTo("João Souza");
  }
}
