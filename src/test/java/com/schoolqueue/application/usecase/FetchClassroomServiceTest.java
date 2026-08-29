package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchClassroomServiceTest {

  @Mock ClassroomRepositoryPort classroomRepositoryPort;

  @Test
  @DisplayName("returns the classroom when it exists")
  void shouldReturnClassroomWhenItExists() {
    UUID id = UUID.randomUUID();
    Classroom classroom = new Classroom(id, UUID.randomUUID(), "Turma A");
    when(classroomRepositoryPort.findById(id)).thenReturn(Optional.of(classroom));

    Classroom result = new FetchClassroomService(classroomRepositoryPort).execute(id);

    assertThat(result).isEqualTo(classroom);
  }
}
