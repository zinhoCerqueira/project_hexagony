package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListStudentsBySchoolServiceTest {

  @Mock StudentRepositoryPort studentRepositoryPort;
  @Mock SchoolRepositoryPort schoolRepositoryPort;

  private ListStudentsBySchoolService newService() {
    return new ListStudentsBySchoolService(studentRepositoryPort, schoolRepositoryPort);
  }

  @Test
  @DisplayName("returns students when the school exists")
  void shouldReturnStudentsWhenSchoolExists() {
    UUID schoolId = UUID.randomUUID();
    Student student = new Student(UUID.randomUUID(), schoolId, UUID.randomUUID(), "A");
    when(schoolRepositoryPort.findById(schoolId))
        .thenReturn(
            Optional.of(new School(schoolId, "S", new BigDecimal("0"), new BigDecimal("0"))));
    when(studentRepositoryPort.findBySchoolId(schoolId)).thenReturn(List.of(student));

    List<Student> result = newService().execute(schoolId);

    assertThat(result).containsExactly(student);
    verify(studentRepositoryPort).findBySchoolId(schoolId);
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolMissing() {
    UUID schoolId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> newService().execute(schoolId))
        .isInstanceOf(SchoolNotFoundException.class);
  }
}
