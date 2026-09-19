package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListStudentsByClassroomServiceTest {

  @Mock StudentRepositoryPort studentRepositoryPort;
  @Mock ClassroomRepositoryPort classroomRepositoryPort;

  private ListStudentsByClassroomService newService() {
    return new ListStudentsByClassroomService(studentRepositoryPort, classroomRepositoryPort);
  }

  @Test
  @DisplayName("returns students when the classroom exists")
  void shouldReturnStudentsWhenClassroomExists() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    Student student = new Student(UUID.randomUUID(), schoolId, classroomId, "A");
    when(classroomRepositoryPort.findById(classroomId))
        .thenReturn(Optional.of(new Classroom(classroomId, schoolId, "A")));
    when(studentRepositoryPort.findByClassroomId(classroomId)).thenReturn(List.of(student));

    List<Student> result = newService().execute(classroomId);

    assertThat(result).containsExactly(student);
    verify(studentRepositoryPort).findByClassroomId(classroomId);
  }

  @Test
  @DisplayName("throws ClassroomNotFoundException when the classroom does not exist")
  void shouldThrowWhenClassroomMissing() {
    UUID classroomId = UUID.randomUUID();
    when(classroomRepositoryPort.findById(classroomId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> newService().execute(classroomId))
        .isInstanceOf(ClassroomNotFoundException.class);
  }
}
