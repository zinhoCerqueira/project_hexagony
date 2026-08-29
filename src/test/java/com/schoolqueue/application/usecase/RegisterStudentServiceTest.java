package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase.RegisterStudentCommand;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterStudentServiceTest {

  @Mock StudentRepositoryPort studentRepositoryPort;
  @Mock SchoolRepositoryPort schoolRepositoryPort;
  @Mock ClassroomRepositoryPort classroomRepositoryPort;
  @Mock ParentRepositoryPort parentRepositoryPort;
  @Mock ParentStudentLinkRepositoryPort linkPort;

  private RegisterStudentService newService() {
    return new RegisterStudentService(
        studentRepositoryPort,
        schoolRepositoryPort,
        classroomRepositoryPort,
        parentRepositoryPort,
        linkPort);
  }

  @Test
  @DisplayName("saves student and replaces parent links when all FKs exist")
  void shouldSaveStudentAndLinkParents() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.School(schoolId, "S", new java.math.BigDecimal("0"), new java.math.BigDecimal("0"))));
    when(classroomRepositoryPort.findById(classroomId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.Classroom(classroomId, schoolId, "A")));
    when(parentRepositoryPort.findById(parentId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.Parent(parentId, "P", "1")));
    when(studentRepositoryPort.save(any(Student.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Student result =
        newService()
            .execute(new RegisterStudentCommand(schoolId, classroomId, "João", List.of(parentId)));

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().id()).isNotNull();
    assertThat(result.name()).isEqualTo("João");
    verify(linkPort).replaceParentsOfStudent(result.id(), List.of(parentId));
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolMissing() {
    UUID schoolId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () -> newService().execute(new RegisterStudentCommand(schoolId, UUID.randomUUID(), "x", List.of(UUID.randomUUID()))))
        .isInstanceOf(SchoolNotFoundException.class);
  }

  @Test
  @DisplayName("throws ClassroomNotFoundException when the classroom does not exist")
  void shouldThrowWhenClassroomMissing() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.School(schoolId, "S", new java.math.BigDecimal("0"), new java.math.BigDecimal("0"))));
    when(classroomRepositoryPort.findById(classroomId)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () -> newService().execute(new RegisterStudentCommand(schoolId, classroomId, "x", List.of(UUID.randomUUID()))))
        .isInstanceOf(ClassroomNotFoundException.class);
  }

  @Test
  @DisplayName("throws ParentNotFoundException when a parent does not exist")
  void shouldThrowWhenParentMissing() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.School(schoolId, "S", new java.math.BigDecimal("0"), new java.math.BigDecimal("0"))));
    when(classroomRepositoryPort.findById(classroomId)).thenReturn(Optional.of(new com.schoolqueue.domain.model.Classroom(classroomId, schoolId, "A")));
    when(parentRepositoryPort.findById(parentId)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () -> newService().execute(new RegisterStudentCommand(schoolId, classroomId, "x", List.of(parentId))))
        .isInstanceOf(ParentNotFoundException.class);
  }
}
