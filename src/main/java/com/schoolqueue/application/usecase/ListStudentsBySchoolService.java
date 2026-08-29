package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.ListStudentsBySchoolUseCase;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.UUID;

public class ListStudentsBySchoolService implements ListStudentsBySchoolUseCase {

  private final StudentRepositoryPort studentRepositoryPort;

  public ListStudentsBySchoolService(StudentRepositoryPort studentRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
  }

  @Override
  public List<Student> execute(UUID schoolId) {
    return studentRepositoryPort.findBySchoolId(schoolId);
  }
}
