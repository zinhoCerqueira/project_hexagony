package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ClassroomEntity;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClassroomEntityMapperTest {

  @Test
  @DisplayName("roundtrip preserves all fields")
  void shouldRoundtripPreserveFields() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    Classroom domain = new Classroom(id, schoolId, "Turma A");

    ClassroomEntity entity = ClassroomEntityMapper.toEntity(domain);
    Classroom back = ClassroomEntityMapper.toDomain(entity);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getSchoolId()).isEqualTo(schoolId);
    assertThat(entity.getName()).isEqualTo("Turma A");
    assertThat(back.id()).isEqualTo(id);
    assertThat(back.schoolId()).isEqualTo(schoolId);
    assertThat(back.name()).isEqualTo("Turma A");
  }
}
