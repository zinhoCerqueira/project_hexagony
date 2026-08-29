package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.StudentEntity;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudentEntityMapperTest {

  @Test
  @DisplayName("roundtrip preserves all fields")
  void shouldRoundtripPreserveFields() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    Student domain = new Student(id, schoolId, classroomId, "João");

    StudentEntity entity = StudentEntityMapper.toEntity(domain);
    Student back = StudentEntityMapper.toDomain(entity);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getSchoolId()).isEqualTo(schoolId);
    assertThat(entity.getClassroomId()).isEqualTo(classroomId);
    assertThat(entity.getName()).isEqualTo("João");
    assertThat(back.id()).isEqualTo(id);
    assertThat(back.schoolId()).isEqualTo(schoolId);
    assertThat(back.classroomId()).isEqualTo(classroomId);
    assertThat(back.name()).isEqualTo("João");
  }
}
