package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase.UpdateClassroomCommand;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateClassroomServiceTest {

  @Mock ClassroomRepositoryPort classroomRepositoryPort;
  @Mock SchoolRepositoryPort schoolRepositoryPort;

  @Test
  @DisplayName("updates an existing classroom when the school exists")
  void shouldUpdateClassroomWhenSchoolExists() {
    UUID classroomId = UUID.randomUUID();
    UUID oldSchoolId = UUID.randomUUID();
    UUID newSchoolId = UUID.randomUUID();
    Classroom existing = new Classroom(classroomId, oldSchoolId, "Old");
    when(classroomRepositoryPort.findById(classroomId)).thenReturn(Optional.of(existing));
    when(schoolRepositoryPort.findById(newSchoolId))
        .thenReturn(
            Optional.of(
                new com.schoolqueue.domain.model.School(
                    newSchoolId, "X", new java.math.BigDecimal("0"), new java.math.BigDecimal("0"))));
    when(classroomRepositoryPort.save(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateClassroomCommand command =
        new UpdateClassroomCommand(classroomId, newSchoolId, "New Name");
    Classroom result =
        new UpdateClassroomService(classroomRepositoryPort, schoolRepositoryPort).execute(command);

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepositoryPort).save(captor.capture());
    Classroom saved = captor.getValue();
    assertThat(saved.schoolId()).isEqualTo(newSchoolId);
    assertThat(saved.name()).isEqualTo("New Name");
    assertThat(result.name()).isEqualTo("New Name");
  }

  @Test
  @DisplayName("throws ClassroomNotFoundException when the classroom does not exist")
  void shouldThrowWhenClassroomDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(classroomRepositoryPort.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                new UpdateClassroomService(classroomRepositoryPort, schoolRepositoryPort)
                    .execute(new UpdateClassroomCommand(id, UUID.randomUUID(), "X")))
        .isInstanceOf(ClassroomNotFoundException.class);
  }
}
