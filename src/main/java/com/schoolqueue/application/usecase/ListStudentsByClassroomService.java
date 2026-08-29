package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.ListStudentsByClassroomUseCase;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.UUID;

public class ListStudentsByClassroomService implements ListStudentsByClassroomUseCase {

  private final StudentRepositoryPort studentRepositoryPort;

  public ListStudentsByClassroomService(StudentRepositoryPort studentRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
  }

  @Override
  public List<Student> execute(UUID classroomId) {
    return studentRepositoryPort.findByClassroomId(classroomId);
  }
}
