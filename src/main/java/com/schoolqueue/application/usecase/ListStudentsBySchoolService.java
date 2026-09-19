package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.ListStudentsBySchoolUseCase;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.UUID;

public class ListStudentsBySchoolService implements ListStudentsBySchoolUseCase {

  private final StudentRepositoryPort studentRepositoryPort;
  private final SchoolRepositoryPort schoolRepositoryPort;

  public ListStudentsBySchoolService(
      StudentRepositoryPort studentRepositoryPort, SchoolRepositoryPort schoolRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public List<Student> execute(UUID schoolId) {
    if (schoolRepositoryPort.findById(schoolId).isEmpty()) {
      throw new SchoolNotFoundException("Escola não encontrada");
    }
    return studentRepositoryPort.findBySchoolId(schoolId);
  }
}
