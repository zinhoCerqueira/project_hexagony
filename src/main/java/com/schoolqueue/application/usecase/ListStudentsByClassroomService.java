package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.ListStudentsByClassroomUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.List;
import java.util.UUID;

public class ListStudentsByClassroomService implements ListStudentsByClassroomUseCase {

  private final StudentRepositoryPort studentRepositoryPort;
  private final ClassroomRepositoryPort classroomRepositoryPort;

  public ListStudentsByClassroomService(
      StudentRepositoryPort studentRepositoryPort,
      ClassroomRepositoryPort classroomRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
    this.classroomRepositoryPort = classroomRepositoryPort;
  }

  @Override
  public List<Student> execute(UUID classroomId) {
    if (classroomRepositoryPort.findById(classroomId).isEmpty()) {
      throw new ClassroomNotFoundException("Turma não encontrada");
    }
    return studentRepositoryPort.findByClassroomId(classroomId);
  }
}
