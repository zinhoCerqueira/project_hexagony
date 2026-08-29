package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase.RegisterClassroomCommand;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterClassroomServiceTest {

  @Mock SchoolRepositoryPort schoolRepositoryPort;
  @Mock ClassroomRepositoryPort classroomRepositoryPort;

  @Test
  @DisplayName("saves the classroom when the school exists")
  void shouldSaveClassroomWhenSchoolExists() {
    UUID schoolId = UUID.randomUUID();
    School school =
        new School(schoolId, "Escola", new BigDecimal("-23.5"), new BigDecimal("-46.6"));
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.of(school));
    when(classroomRepositoryPort.save(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisterClassroomCommand command = new RegisterClassroomCommand(schoolId, "Turma A");
    Classroom result =
        new RegisterClassroomService(schoolRepositoryPort, classroomRepositoryPort).execute(command);

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().schoolId()).isEqualTo(schoolId);
    assertThat(captor.getValue().name()).isEqualTo("Turma A");
    assertThat(result.name()).isEqualTo("Turma A");
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolDoesNotExist() {
    UUID schoolId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.empty());
    RegisterClassroomCommand command = new RegisterClassroomCommand(schoolId, "Turma A");

    assertThatThrownBy(
            () ->
                new RegisterClassroomService(schoolRepositoryPort, classroomRepositoryPort)
                    .execute(command))
        .isInstanceOf(SchoolNotFoundException.class);
  }
}
