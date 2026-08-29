package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListClassroomsBySchoolServiceTest {

  @Mock ClassroomRepositoryPort classroomRepositoryPort;

  @Test
  @DisplayName("returns the classrooms of the given school")
  void shouldReturnClassroomsBySchool() {
    UUID schoolId = UUID.randomUUID();
    Classroom c1 = new Classroom(UUID.randomUUID(), schoolId, "A");
    Classroom c2 = new Classroom(UUID.randomUUID(), schoolId, "B");
    when(classroomRepositoryPort.findBySchoolId(schoolId)).thenReturn(List.of(c1, c2));

    List<Classroom> result = new ListClassroomsBySchoolService(classroomRepositoryPort).execute(schoolId);

    assertThat(result).containsExactly(c1, c2);
  }
}
