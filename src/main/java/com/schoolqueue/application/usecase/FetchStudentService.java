package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.StudentNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.FetchStudentUseCase;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import java.util.UUID;

public class FetchStudentService implements FetchStudentUseCase {

  private final StudentRepositoryPort studentRepositoryPort;

  public FetchStudentService(StudentRepositoryPort studentRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
  }

  @Override
  public Student execute(UUID id) {
    return studentRepositoryPort
        .findById(id)
        .orElseThrow(() -> new StudentNotFoundException("Aluno não encontrado"));
  }
}
