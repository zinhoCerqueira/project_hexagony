package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.FetchClassroomUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import java.util.UUID;

public class FetchClassroomService implements FetchClassroomUseCase {

  private final ClassroomRepositoryPort classroomRepositoryPort;

  public FetchClassroomService(ClassroomRepositoryPort classroomRepositoryPort) {
    this.classroomRepositoryPort = classroomRepositoryPort;
  }

  @Override
  public Classroom execute(UUID id) {
    return classroomRepositoryPort
        .findById(id)
        .orElseThrow(() -> new ClassroomNotFoundException("Turma não encontrada"));
  }
}
